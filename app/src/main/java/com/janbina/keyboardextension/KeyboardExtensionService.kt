package com.janbina.keyboardextension

import android.accessibilityservice.AccessibilityService
import android.graphics.PixelFormat
import android.graphics.Rect
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityWindowInfo
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.core.content.getSystemService
import androidx.core.os.bundleOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewTreeLifecycleOwner
import androidx.lifecycle.ViewTreeViewModelStoreOwner
import androidx.savedstate.ViewTreeSavedStateRegistryOwner


class KeyboardExtensionService : AccessibilityService() {

    private val windowManager get() = getSystemService<WindowManager>()

    private var view: View? = null
    private var lastEditText: AccessibilityNodeInfo? = null
    private var buttonsVisible = false
    private var contentBeforeModification: String? = null

    private val baseLayoutParams = WindowManager.LayoutParams().apply {
        type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
        format = PixelFormat.TRANSLUCENT
        flags = flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        width = WindowManager.LayoutParams.WRAP_CONTENT
        height = WindowManager.LayoutParams.WRAP_CONTENT
        gravity = Gravity.BOTTOM or Gravity.END
    }

    override fun onServiceConnected() {
        createButtons()

        serviceInfo = serviceInfo.apply {
            eventTypes = AccessibilityEvent.TYPE_VIEW_FOCUSED or
                    AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED or
                    AccessibilityEvent.TYPE_WINDOWS_CHANGED
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOWS_CHANGED -> updateKeyboardState()
            AccessibilityEvent.TYPE_VIEW_FOCUSED,
            AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED -> updateEditText(event)
            else -> Unit
        }
    }

    override fun onInterrupt() {
        view?.let { windowManager?.removeView(it) }
    }

    private fun createButtons() {
        val view = ComposeView(this)
        view.setContent {
            KeyboardExtensionTheme {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        icon = Icons.Default.Undo,
                        onClick = { undo() },
                    )
                    Button(
                        icon = Icons.Default.Assignment,
                        onClick = { paste() },
                    )
                    Button(
                        icon = Icons.Default.Translate,
                        onClick = { modify() },
                    )
                }
            }
        }

        // trick from https://gist.github.com/handstandsam/6ecff2f39da72c0b38c07aa80bbb5a2f
        // Trick The ComposeView into thinking we are tracking lifecycle
        val viewModelStore = ViewModelStore()
        val lifecycleOwner = MyLifecycleOwner()
        lifecycleOwner.performRestore(null)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        ViewTreeLifecycleOwner.set(view, lifecycleOwner)
        ViewTreeViewModelStoreOwner.set(view) { viewModelStore }
        ViewTreeSavedStateRegistryOwner.set(view, lifecycleOwner)

        this.view = view
    }

    private fun updateEditText(event: AccessibilityEvent) {
        if (event.className == "android.widget.EditText" && event.source != null) {
            lastEditText = event.source
        }
    }

    private fun updateKeyboardState() {
        val keyboard = windows.firstOrNull {
            it.type == AccessibilityWindowInfo.TYPE_INPUT_METHOD
        }

        if (keyboard != null && !buttonsVisible) { //show
            val keyboardHeight = Rect().also { keyboard.getBoundsInScreen(it) }.height()
            val params = baseLayoutParams.apply {
                y = keyboardHeight
            }
            windowManager?.addView(view, params)
            buttonsVisible = true
        }
        if (keyboard == null && buttonsVisible) { //hide
            view?.let { windowManager?.removeView(it) }
            buttonsVisible = false
        }
    }

    private fun undo() {
        val editText = lastEditText ?: return
        val newText = contentBeforeModification ?: return

        editText.modifyText { before ->
            contentBeforeModification = before
            newText
        }
    }

    private fun paste() {
        val editText = lastEditText ?: return
        contentBeforeModification = editText.text.toString()
        editText.performAction(AccessibilityNodeInfo.ACTION_PASTE);
    }

    private fun modify() {
        val editText = lastEditText ?: return

        editText.modifyText { before ->
            contentBeforeModification = before
            before.lowercase().removeAccents()
        }
    }

    companion object {
        private const val LogTag = "TextModifierService"
    }
}

private fun AccessibilityNodeInfo.modifyText(
    modification: (String) -> String,
) {
    val modified = modification(text.toString())

    performAction(
        AccessibilityNodeInfo.ACTION_SET_TEXT,
        bundleOf(
            AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE to modified,
        )
    )
}

@Composable
private fun Button(
    icon: ImageVector,
    onClick: () -> Unit,
) {
    FilledTonalButton(
        contentPadding = PaddingValues(0.dp),
        onClick = onClick,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null, // should accessibility service care about accessibility?
        )
    }
}
