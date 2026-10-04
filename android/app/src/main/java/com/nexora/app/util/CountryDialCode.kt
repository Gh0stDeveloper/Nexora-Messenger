package com.nexora.app.util

data class CountryDialCode(
    val iso: String,
    val flag: String,
    val name: String,
    val dialCode: String,
)

object CountryDialCodes {
    val all: List<CountryDialCode> = listOf(
        CountryDialCode("AF", "🇦🇫", "Afganistán", "+93"),
        CountryDialCode("AL", "🇦🇱", "Albania", "+355"),
        CountryDialCode("DZ", "🇩🇿", "Argelia", "+213"),
        CountryDialCode("AD", "🇦🇩", "Andorra", "+376"),
        CountryDialCode("AO", "🇦🇴", "Angola", "+244"),
        CountryDialCode("AR", "🇦🇷", "Argentina", "+54"),
        CountryDialCode("AM", "🇦🇲", "Armenia", "+374"),
        CountryDialCode("AU", "🇦🇺", "Australia", "+61"),
        CountryDialCode("AT", "🇦🇹", "Austria", "+43"),
        CountryDialCode("AZ", "🇦🇿", "Azerbaiyán", "+994"),
        CountryDialCode("BS", "🇧🇸", "Bahamas", "+1"),
        CountryDialCode("BH", "🇧🇭", "Baréin", "+973"),
        CountryDialCode("BD", "🇧🇩", "Bangladesh", "+880"),
        CountryDialCode("BE", "🇧🇪", "Bélgica", "+32"),
        CountryDialCode("BZ", "🇧🇿", "Belice", "+501"),
        CountryDialCode("BO", "🇧🇴", "Bolivia", "+591"),
        CountryDialCode("BR", "🇧🇷", "Brasil", "+55"),
        CountryDialCode("CA", "🇨🇦", "Canadá", "+1"),
        CountryDialCode("CL", "🇨🇱", "Chile", "+56"),
        CountryDialCode("CN", "🇨🇳", "China", "+86"),
        CountryDialCode("CO", "🇨🇴", "Colombia", "+57"),
        CountryDialCode("CR", "🇨🇷", "Costa Rica", "+506"),
        CountryDialCode("CU", "🇨🇺", "Cuba", "+53"),
        CountryDialCode("DK", "🇩🇰", "Dinamarca", "+45"),
        CountryDialCode("DO", "🇩🇴", "República Dominicana", "+1"),
        CountryDialCode("EC", "🇪🇨", "Ecuador", "+593"),
        CountryDialCode("EG", "🇪🇬", "Egipto", "+20"),
        CountryDialCode("SV", "🇸🇻", "El Salvador", "+503"),
        CountryDialCode("FI", "🇫🇮", "Finlandia", "+358"),
        CountryDialCode("FR", "🇫🇷", "Francia", "+33"),
        CountryDialCode("DE", "🇩🇪", "Alemania", "+49"),
        CountryDialCode("GR", "🇬🇷", "Grecia", "+30"),
        CountryDialCode("GT", "🇬🇹", "Guatemala", "+502"),
        CountryDialCode("HN", "🇭🇳", "Honduras", "+504"),
        CountryDialCode("HK", "🇭🇰", "Hong Kong", "+852"),
        CountryDialCode("IN", "🇮🇳", "India", "+91"),
        CountryDialCode("ID", "🇮🇩", "Indonesia", "+62"),
        CountryDialCode("IE", "🇮🇪", "Irlanda", "+353"),
        CountryDialCode("IL", "🇮🇱", "Israel", "+972"),
        CountryDialCode("IT", "🇮🇹", "Italia", "+39"),
        CountryDialCode("JM", "🇯🇲", "Jamaica", "+1"),
        CountryDialCode("JP", "🇯🇵", "Japón", "+81"),
        CountryDialCode("KR", "🇰🇷", "Corea del Sur", "+82"),
        CountryDialCode("LU", "🇱🇺", "Luxemburgo", "+352"),
        CountryDialCode("MY", "🇲🇾", "Malasia", "+60"),
        CountryDialCode("MX", "🇲🇽", "México", "+52"),
        CountryDialCode("MA", "🇲🇦", "Marruecos", "+212"),
        CountryDialCode("NL", "🇳🇱", "Países Bajos", "+31"),
        CountryDialCode("NZ", "🇳🇿", "Nueva Zelanda", "+64"),
        CountryDialCode("NI", "🇳🇮", "Nicaragua", "+505"),
        CountryDialCode("NO", "🇳🇴", "Noruega", "+47"),
        CountryDialCode("PA", "🇵🇦", "Panamá", "+507"),
        CountryDialCode("PY", "🇵🇾", "Paraguay", "+595"),
        CountryDialCode("PE", "🇵🇪", "Perú", "+51"),
        CountryDialCode("PH", "🇵🇭", "Filipinas", "+63"),
        CountryDialCode("PL", "🇵🇱", "Polonia", "+48"),
        CountryDialCode("PT", "🇵🇹", "Portugal", "+351"),
        CountryDialCode("PR", "🇵🇷", "Puerto Rico", "+1"),
        CountryDialCode("QA", "🇶🇦", "Catar", "+974"),
        CountryDialCode("RO", "🇷🇴", "Rumania", "+40"),
        CountryDialCode("SA", "🇸🇦", "Arabia Saudita", "+966"),
        CountryDialCode("SG", "🇸🇬", "Singapur", "+65"),
        CountryDialCode("ZA", "🇿🇦", "Sudáfrica", "+27"),
        CountryDialCode("ES", "🇪🇸", "España", "+34"),
        CountryDialCode("SE", "🇸🇪", "Suecia", "+46"),
        CountryDialCode("CH", "🇨🇭", "Suiza", "+41"),
        CountryDialCode("TW", "🇹🇼", "Taiwán", "+886"),
        CountryDialCode("TH", "🇹🇭", "Tailandia", "+66"),
        CountryDialCode("TR", "🇹🇷", "Turquía", "+90"),
        CountryDialCode("UA", "🇺🇦", "Ucrania", "+380"),
        CountryDialCode("AE", "🇦🇪", "Emiratos Árabes Unidos", "+971"),
        CountryDialCode("GB", "🇬🇧", "Reino Unido", "+44"),
        CountryDialCode("US", "🇺🇸", "Estados Unidos", "+1"),
        CountryDialCode("UY", "🇺🇾", "Uruguay", "+598"),
        CountryDialCode("VE", "🇻🇪", "Venezuela", "+58"),
        CountryDialCode("VN", "🇻🇳", "Vietnam", "+84")
    ).sortedBy { it.name }

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
    if (digits.isBlank()) return ""
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

fun nationalPhoneInput(country: CountryDialCode, input: String): String {
    val digits = input.filter(Char::isDigit)
    val countryDigits = country.dialCode.filter(Char::isDigit)
    return when {
        country.iso == "MX" && digits.startsWith("521") && digits.length >= 13 -> digits.drop(3)
        country.iso == "MX" && digits.startsWith("52") && digits.length >= 12 -> digits.drop(2)
        country.iso == "MX" && digits.startsWith("1") && digits.length == 11 -> digits.drop(1)
        digits.startsWith(countryDigits) && digits.length > countryDigits.length + 4 -> digits.drop(countryDigits.length)
        else -> digits
    }
}
