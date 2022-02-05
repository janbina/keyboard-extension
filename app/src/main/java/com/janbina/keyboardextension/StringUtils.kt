package com.janbina.keyboardextension

private val accentSubstitutions = listOf(
    "ÀÁÂÃÄÅǺĀĂĄǍΑΆẢẠẦẪẨẬẰẮẴẲẶА" to 'A',
    "àáâãåǻāăąǎªαάảạầấẫẩậằắẵẳặа" to 'a',
    "ÇĆĈĊČ" to 'C',
    "çćĉċč" to 'c',
    "ÐĎĐΔ" to 'D',
    "ðďđδ" to 'd',
    "ÈÉÊËĒĔĖĘĚΕΈẼẺẸỀẾỄỂỆЕЭ" to 'E',
    "èéêëēĕėęěέεẽẻẹềếễểệеэ" to 'e',
    "ÌÍÎÏĨĪĬǏĮİΗΉΊΙΪỈỊИЫ" to 'I',
    "ìíîïĩīĭǐįıηήίιϊỉịиыї" to 'i',
    "ĹĻĽĿŁΛЛ" to 'L',
    "ĺļľŀłλл" to 'l',
    "ÑŃŅŇΝН" to 'N',
    "ñńņňŉνн" to 'n',
    "ÒÓÔÕŌŎǑŐƠØǾΟΌΩΏỎỌỒỐỖỔỘỜỚỠỞỢО" to 'O',
    "òóôõōŏǒőơøǿºοόωώỏọồốỗổộờớỡởợо" to 'o',
    "ŔŖŘΡР" to 'R',
    "ŕŗřρр" to 'r',
    "ŚŜŞȘŠΣС" to 'S',
    "śŝşșšſσςс" to 's',
    "ȚŢŤŦτТ" to 'T',
    "țţťŧт" to 't',
    "ÙÚÛŨŪŬŮŰŲƯǓǕǗǙǛŨỦỤỪỨỮỬỰУ" to 'U',
    "ùúûũūŭůűųưǔǖǘǚǜυύϋủụừứữửựу" to 'u',
    "ÝŸŶΥΎΫỲỸỶỴЙ" to 'Y',
    "ýÿŷỳỹỷỵй" to 'y',
    "ŹŻŽΖЗ" to 'Z',
    "źżžζз" to 'z',
).flatMap { pair ->
    pair.first.map { c -> c to pair.second }
}.toMap()

fun String.removeAccents(): String {
    return map { accentSubstitutions[it] ?: it }.joinToString(separator = "")
}
