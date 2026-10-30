package com.example.data.ads

data class CompanyBannerAd(
    val id: String,
    val companyName: String,
    val brandColorHex: Long,
    val logoMonogram: String,
    val headline: String,
    val promoOffer: String,
    val ctaText: String,
    val sector: String,
    val partnerTier: String
)

object AdNetworkManager {

    private val partnerBanners = listOf(
        CompanyBannerAd(
            id = "ad_ultratech",
            companyName = "UltraTech Cement",
            brandColorHex = 0xFFD32F2F,
            logoMonogram = "UT",
            headline = "The Engineer's Choice • Bulk Site Delivery",
            promoOffer = "Get 8% Rebate on Grade 53 Bulk Orders",
            ctaText = "Order Bulk",
            sector = "Cement & Concrete",
            partnerTier = "Diamond Sponsor"
        ),
        CompanyBannerAd(
            id = "ad_tatasteel",
            companyName = "Tata Steel Tiscon",
            brandColorHex = 0xFF0D47A1,
            logoMonogram = "TS",
            headline = "550D Super Ductile Rebars for NCR Projects",
            promoOffer = "Zero-Cost Freight on 20+ Tonne Loads",
            ctaText = "Get Quote",
            sector = "Structural Steel",
            partnerTier = "Platinum Partner"
        ),
        CompanyBannerAd(
            id = "ad_havells",
            companyName = "Havells Professional",
            brandColorHex = 0xFFC62828,
            logoMonogram = "HV",
            headline = "Flame-Retardant Industrial Wiring & Switchgear",
            promoOffer = "Flat 12% Contractor Discount on MCB/DB Panels",
            ctaText = "View Catalog",
            sector = "Electrical & Lighting",
            partnerTier = "Gold Partner"
        ),
        CompanyBannerAd(
            id = "ad_jcb",
            companyName = "JCB India Equipment",
            brandColorHex = 0xFFF57F17,
            logoMonogram = "JCB",
            headline = "3DX EcoXcellence Backhoe Loaders on Site Lease",
            promoOffer = "1st Month 50% Rental Subsidy for Verified Firms",
            ctaText = "Rent Now",
            sector = "Heavy Earthmoving Machinery",
            partnerTier = "Diamond Sponsor"
        ),
        CompanyBannerAd(
            id = "ad_asianpaints",
            companyName = "Asian Paints SmartCare",
            brandColorHex = 0xFFE65100,
            logoMonogram = "AP",
            headline = "DampBlock 2K Advanced Waterproofing Solution",
            promoOffer = "Complimentary Site Inspection & 10-Yr Warranty",
            ctaText = "Book Demo",
            sector = "Paints & Waterproofing",
            partnerTier = "Platinum Partner"
        ),
        CompanyBannerAd(
            id = "ad_bosch",
            companyName = "Bosch Professional Tools",
            brandColorHex = 0xFF00695C,
            logoMonogram = "BSH",
            headline = "Heavy Duty Cordless Drills, Grinders & Breakers",
            promoOffer = "Free 1-Yr Extended Tool Warranty + 2 Extra Batteries",
            ctaText = "Shop Tools",
            sector = "Power Tools & PPE",
            partnerTier = "Gold Partner"
        ),
        CompanyBannerAd(
            id = "ad_kamdhenu",
            companyName = "Kamdhenu Steel & Paints",
            brandColorHex = 0xFF4A148C,
            logoMonogram = "KD",
            headline = "NXT Double Ribbed TMT Bars & Structural Sections",
            promoOffer = "Instant Credit Terms Available for Sub-Contractors",
            ctaText = "Enquire",
            sector = "Structural Steel",
            partnerTier = "Silver Partner"
        ),
        CompanyBannerAd(
            id = "ad_hilti",
            companyName = "Hilti Fasteners & Anchors",
            brandColorHex = 0xFFB71C1C,
            logoMonogram = "HT",
            headline = "Direct Fastening, Diamond Coring & Firestop Tech",
            promoOffer = "Free On-Site Pull-Out Testing & Structural Reports",
            ctaText = "Request Test",
            sector = "Fasteners & Fixings",
            partnerTier = "Platinum Partner"
        ),
        CompanyBannerAd(
            id = "ad_schneider",
            companyName = "Schneider Electric",
            brandColorHex = 0xFF2E7D32,
            logoMonogram = "SE",
            headline = "Acti9 Safe Energy Distribution & Smart Meters",
            promoOffer = "20% Cashback on Solar Inverter Integration",
            ctaText = "Explore",
            sector = "Energy Management",
            partnerTier = "Gold Partner"
        ),
        CompanyBannerAd(
            id = "ad_pidilite",
            companyName = "Dr. Fixit (Pidilite)",
            brandColorHex = 0xFFF9A825,
            logoMonogram = "DF",
            headline = "Complete Leak-Proof Concrete Additives & Sealants",
            promoOffer = "Buy 5 Barrels of LW+, Get 1 FastFlex Barrel Free",
            ctaText = "Claim Deal",
            sector = "Chemicals & Adhesives",
            partnerTier = "Gold Partner"
        )
    )

    fun getRandomBanner(): CompanyBannerAd {
        return partnerBanners.random()
    }

    fun getAdByIndex(index: Int): CompanyBannerAd {
        val safeIndex = Math.floorMod(index, partnerBanners.size)
        return partnerBanners[safeIndex]
    }

    fun getAllBanners(): List<CompanyBannerAd> = partnerBanners
}
