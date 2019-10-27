package com.github.mikucat0309.commandsystem.locale

import java.util.*

/**
 * The list of locales available in Vanilla Minecraft.
 */
class Locales// Suppress default constructor to ensure non-instantiability.
private constructor() {

    init {
        throw AssertionError("You should not be attempting to instantiate this class.")
    }

    companion object {
        val AF_ZA = Locale("af", "ZA")
        val AR_SA = Locale("ar", "SA")
        val AST_ES = Locale("ast", "ES")
        val AZ_AZ = Locale("az", "AZ")
        val BG_BG = Locale("bg", "BG")
        val CA_ES = Locale("ca", "ES")
        val CS_CZ = Locale("cs", "CZ")
        val CY_GB = Locale("cy", "GB")
        val DA_DK = Locale("da", "DK")
        val DE_DE = Locale.GERMANY
        val EL_GR = Locale("el", "GR")
        val EN_AU = Locale("en", "AU")
        val EN_CA = Locale("en", "CA")
        val EN_GB = Locale.UK
        val EN_PT = Locale("en", "PT")
        val EN_US = Locale.US
        val EO_UY = Locale("eo", "UY")
        val ES_AR = Locale("es", "AR")
        val ES_ES = Locale("es", "ES")
        val ES_MX = Locale("es", "MX")
        val ES_UY = Locale("es", "UY")
        val ES_VE = Locale("es", "VE")
        val ET_EE = Locale("et", "EE")
        val EU_ES = Locale("eu", "ES")
        val FA_IR = Locale("fa", "IR")
        val FI_FI = Locale("fi", "FI")
        val FIL_PH = Locale("fil", "PH")
        val FR_CA = Locale.CANADA_FRENCH
        val FR_FR = Locale.FRANCE
        val GA_IE = Locale("ga", "IE")
        val GL_ES = Locale("gl", "ES")
        val GV_IM = Locale("gv", "IM")
        val HE_IL = Locale("he", "IL")
        val HI_IN = Locale("hi", "IN")
        val HR_HR = Locale("hr", "HR")
        val HU_HU = Locale("hu", "HU")
        val HY_AM = Locale("hy", "AM")
        val ID_ID = Locale("id", "ID")
        val IS_IS = Locale("is", "IS")
        val IT_IT = Locale.ITALY
        val JA_JP = Locale.JAPAN
        val KA_GE = Locale("ka", "GE")
        val KO_KR = Locale.KOREA
        val KW_GB = Locale("kw", "GB")
        val LA_LA = Locale("la", "LA")
        val LB_LU = Locale("lb", "LU")
        val LT_LT = Locale("lt", "LT")
        val LV_LV = Locale("lv", "LV")
        val MI_NZ = Locale("mi", "NZ")
        val MS_MY = Locale("ms", "MY")
        val MT_MT = Locale("mt", "MT")
        val NDS_DE = Locale("nds", "DE")
        val NL_NL = Locale("nl", "NL")
        val NN_NO = Locale("nn", "NO")
        val NO_NO = Locale("no", "NO")
        val OC_FR = Locale("oc", "FR")
        val PL_PL = Locale("pl", "PL")
        val PT_BR = Locale("pt", "BR")
        val PT_PT = Locale("pt", "PT")
        val QYA_AA = Locale("qya", "AA")
        val RO_RO = Locale("ro", "RO")
        val RU_RU = Locale("ru", "RU")
        val SE_NO = Locale("se", "NO")
        val SK_SK = Locale("sk", "SK")
        val SL_SI = Locale("sl", "SI")
        val SR_SP = Locale("sr", "SP")
        val SV_SE = Locale("sv", "SE")
        val TH_TH = Locale("th", "TH")
        val TLH_AA = Locale("tlh", "AA")
        val TR_TR = Locale("tr", "TR")
        val UK_UA = Locale("uk", "UA")
        val VAL_ES = Locale("val", "ES")
        val VI_VN = Locale("vi", "VN")
        val ZH_CN = Locale.SIMPLIFIED_CHINESE
        val ZH_TW = Locale.TRADITIONAL_CHINESE

        /**
         * The default locale used when the receiver's locale is unknown.
         */
        val DEFAULT: Locale = EN_US
    }

}
