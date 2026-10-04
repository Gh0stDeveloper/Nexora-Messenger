package com.nexora.app.util

data class CountryDialCode(
    val iso: String,
    val flag: String,
    val name: String,
    val dialCode: String,
)

object CountryDialCodes {
    val all: List<CountryDialCode> = listOf(
        CountryDialCode("MX", "🇲🇽", "México", "+52"),
        CountryDialCode("US", "🇺🇸", "Estados Unidos", "+1"),
        CountryDialCode("CA", "🇨🇦", "Canadá", "+1"),
        CountryDialCode("ES", "🇪🇸", "España", "+34"),
        CountryDialCode("AR", "🇦🇷", "Argentina", "+54"),
        CountryDialCode("CO", "🇨🇴", "Colombia", "+57"),
        CountryDialCode("CL", "🇨🇱", "Chile", "+56"),
        CountryDialCode("PE", "🇵🇪", "Perú", "+51"),
        CountryDialCode("BR", "🇧🇷", "Brasil", "+55"),
        CountryDialCode("GT", "🇬🇹", "Guatemala", "+502"),
        CountryDialCode("SV", "🇸🇻", "El Salvador", "+503"),
        CountryDialCode("HN", "🇭🇳", "Honduras", "+504"),
        CountryDialCode("NI", "🇳🇮", "Nicaragua", "+505"),
        CountryDialCode("CR", "🇨🇷", "Costa Rica", "+506"),
        CountryDialCode("PA", "🇵🇦", "Panamá", "+507"),
        CountryDialCode("DO", "🇩🇴", "República Dominicana", "+1"),
        CountryDialCode("PR", "🇵🇷", "Puerto Rico", "+1"),
        CountryDialCode("GB", "🇬🇧", "Reino Unido", "+44"),
        CountryDialCode("FR", "🇫🇷", "Francia", "+33"),
        CountryDialCode("DE", "🇩🇪", "Alemania", "+49"),
        CountryDialCode("IT", "🇮🇹", "Italia", "+39"),
        CountryDialCode("JP", "🇯🇵", "Japón", "+81"),
        CountryDialCode("KR", "🇰🇷", "Corea del Sur", "+82"),
        CountryDialCode("CN", "🇨🇳", "China", "+86"),
        CountryDialCode("IN", "🇮🇳", "India", "+91")
    )

    val defaultMexico: CountryDialCode = all.first { it.iso == "MX" }

    fun search(query: String): List<CountryDialCode> {
        val clean = query.trim().lowercase().replace("+", "")
        if (clean.isBlank()) return all
        return all.filter { country ->
            country.name.lowercase().contains(clean) ||
                country.iso.lowercase().contains(clean) ||
                country.dialCode.replace("+", "").contains(clean)
        }
    }
}

fun buildInternationalPhone(country: CountryDialCode, input: String): String {
    val digits = input.filter(Char::isDigit)
    val countryDigits = country.dialCode.filter(Char::isDigit)
    val national = when {
        country.iso == "MX" && digits.startsWith("521") && digits.length >= 13 -> digits.drop(3)
        country.iso == "MX" && digits.startsWith("52") && digits.length >= 12 -> digits.drop(2)
        country.iso == "MX" && digits.startsWith("1") && digits.length == 11 -> digits.drop(1)
        digits.startsWith(countryDigits) && digits.length > countryDigits.length + 4 -> digits.drop(countryDigits.length)
        else -> digits
    }
    return country.dialCode + national
}
