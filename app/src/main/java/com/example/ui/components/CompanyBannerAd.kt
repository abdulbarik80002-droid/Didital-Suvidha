package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ads.AdNetworkManager
import com.example.data.ads.CompanyBannerAd
import com.example.ui.theme.*

@Composable
fun CompanyBannerAdCard(
    currentAd: CompanyBannerAd,
    onRotateNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showOfferDialog by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { showOfferDialog = true }
            .testTag("banner_ad_container"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Network & Sponsor Tag Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "SPONSORED",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• 500+ Partner Ad Network",
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                IconButton(
                    onClick = onRotateNext,
                    modifier = Modifier
                        .size(24.dp)
                        .testTag("btn_rotate_ad")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Rotate Partner Ad",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Main Brand Banner Content with Official Logo Display
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Official Company Logo Badge
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(currentAd.brandColorHex)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = currentAd.logoMonogram,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = currentAd.companyName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Verified Partner",
                            tint = Color(0xFF1976D2),
                            modifier = Modifier.size(13.dp)
                        )
                    }

                    Text(
                        text = currentAd.headline,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 14.sp
                    )

                    Text(
                        text = currentAd.promoOffer,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SuvidhaAmberDark,
                        lineHeight = 13.sp
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                FilledTonalButton(
                    onClick = { showOfferDialog = true },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color(currentAd.brandColorHex).copy(alpha = 0.15f),
                        contentColor = Color(currentAd.brandColorHex)
                    ),
                    modifier = Modifier.testTag("btn_ad_cta")
                ) {
                    Text(text = currentAd.ctaText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showOfferDialog) {
        AlertDialog(
            onDismissRequest = { showOfferDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(currentAd.brandColorHex)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(currentAd.logoMonogram, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(currentAd.companyName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = currentAd.headline,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Special Contractor Offer: ${currentAd.promoOffer}",
                        color = SuvidhaAmberDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    Text(
                        text = "Sector: ${currentAd.sector}\n" +
                                "Tier: ${currentAd.partnerTier}\n" +
                                "Network: Digital Suvidha 500+ Corporate Partner Registry",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showOfferDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = SuvidhaNavy)
                ) {
                    Text("Close")
                }
            }
        )
    }
}
