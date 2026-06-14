package com.example.beamsyncmobile.ui.screens.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CastConnected
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.util.lerp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.beamsyncmobile.R
import com.example.beamsyncmobile.ui.components.BeamsyncButton
import com.example.beamsyncmobile.ui.components.BeamsyncButtonSize
import com.example.beamsyncmobile.ui.components.BeamsyncButtonVariant
import com.example.beamsyncmobile.ui.theme.BeamsyncColors
import com.example.beamsyncmobile.ui.theme.BeamsyncSpacing
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    var visiblePage by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(pagerState.currentPage) {
        visiblePage = pagerState.currentPage
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BeamsyncColors.surfaceBase),
    ) {
        BackgroundGrid()

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            pageSpacing = 0.dp,
            beyondViewportPageCount = 1,
        ) { page ->
            val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
            PageContent(
                page = page,
                isVisible = page == visiblePage,
                pageOffset = pageOffset,
            )
        }

        AnimatedVisibility(
            visible = pagerState.currentPage < 2,
            enter = fadeIn(tween(300)),
            exit = fadeOut(tween(200)),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 52.dp, end = BeamsyncSpacing.space4),
        ) {
            Text(
                text = "SKIP",
                color = BeamsyncColors.textSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.5.sp,
                modifier = Modifier
                    .clickable(onClick = onComplete)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = BeamsyncSpacing.space8)
                .padding(bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val isLastPage = pagerState.currentPage == 2

            PageIndicator(
                pageCount = 3,
                currentPage = pagerState.currentPage,
                modifier = Modifier.padding(bottom = BeamsyncSpacing.space6),
            )

            BeamsyncButton(
                text = if (isLastPage) "GET STARTED" else "CONTINUE",
                variant = BeamsyncButtonVariant.Primary,
                size = BeamsyncButtonSize.Large,
                fullWidth = true,
                onClick = {
                    if (isLastPage) {
                        onComplete()
                    } else {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    }
                },
            )
        }
    }
}

@Composable
private fun BackgroundGrid() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        repeat(3) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .size(1.dp)
                            .background(BeamsyncColors.surfaceHigher.copy(alpha = 0.3f)),
                    )
                }
            }
        }
    }
}

@Composable
private fun PageContent(page: Int, isVisible: Boolean, pageOffset: Float) {
    val pages = listOf(
        OnboardingPage(
            icon = {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .background(BeamsyncColors.surfaceRaised, RoundedCornerShape(0.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_onboarding_logo),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(100.dp),
                    )
                }
            },
            title = "WELCOME TO",
            titleHighlight = "BEAMSYNC",
            subtitle = "Transfer files between your phone and PC wirelessly. Fast, secure, and dead simple.",
        ),
        OnboardingPage(
            icon = {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .background(BeamsyncColors.surfaceRaised, RoundedCornerShape(0.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.CastConnected,
                        contentDescription = null,
                        tint = BeamsyncColors.accentPrimary,
                        modifier = Modifier.size(72.dp),
                    )
                }
            },
            title = "SAME",
            titleHighlight = "NETWORK",
            subtitle = "Connect your phone to the same WiFi network as your desktop computer. They need to see each other.",
        ),
        OnboardingPage(
            icon = {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .background(BeamsyncColors.surfaceRaised, RoundedCornerShape(0.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(modifier = Modifier.size(72.dp), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.NetworkCheck,
                            contentDescription = null,
                            tint = BeamsyncColors.accentSecondary,
                            modifier = Modifier.size(72.dp),
                        )
                    }
                }
            },
            title = "5GHz",
            titleHighlight = "HOTSPOT",
            subtitle = "If using a mobile hotspot, switch the band to 5GHz in your phone's settings for maximum transfer speeds.",
        ),
    )

    val data = pages[page]
    val pageProgress = (1f - pageOffset.absoluteValue).coerceIn(0f, 1f)

    val enterScale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.85f,
        animationSpec = tween(500),
        label = "enterScale",
    )
    val iconAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(400),
        label = "iconAlpha",
    )
    val titleAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(400, delayMillis = 150),
        label = "titleAlpha",
    )
    val subtitleAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(400, delayMillis = 300),
        label = "subtitleAlpha",
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                alpha = lerp(0.7f, 1f, pageProgress)
                cameraDistance = 12f * density
            }
            .padding(horizontal = BeamsyncSpacing.space8)
            .padding(top = 100.dp, bottom = 140.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .scale(enterScale)
                .alpha(iconAlpha),
        ) {
            data.icon()
        }

        Spacer(Modifier.height(BeamsyncSpacing.space8))

        Text(
            text = data.title,
            color = BeamsyncColors.textSecondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            letterSpacing = 3.sp,
            modifier = Modifier.alpha(titleAlpha),
        )

        Spacer(Modifier.height(BeamsyncSpacing.space1))

        Text(
            text = data.titleHighlight,
            color = BeamsyncColors.textPrimary,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            lineHeight = 36.sp,
            letterSpacing = -.5.sp,
            modifier = Modifier.alpha(titleAlpha),
        )

        Spacer(Modifier.height(BeamsyncSpacing.space4))

        Text(
            text = data.subtitle,
            color = BeamsyncColors.textSecondary,
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
            modifier = Modifier.alpha(subtitleAlpha),
        )
    }
}

@Composable
private fun PageIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pageCount) { index ->
            val isSelected = index == currentPage
            val animatedWidth by animateFloatAsState(
                targetValue = if (isSelected) 28f else 8f,
                animationSpec = tween(300),
                label = "dotWidth",
            )
            val animatedAlpha by animateFloatAsState(
                targetValue = if (isSelected) 1f else 0.35f,
                animationSpec = tween(300),
                label = "dotAlpha",
            )
            Box(
                modifier = Modifier
                    .width(animatedWidth.dp)
                    .height(3.dp)
                    .alpha(animatedAlpha)
                    .background(
                        if (isSelected) BeamsyncColors.accentPrimary
                        else BeamsyncColors.strokeDefault,
                        RoundedCornerShape(0.dp),
                    ),
            )
        }
    }
}

private data class OnboardingPage(
    val icon: @Composable () -> Unit,
    val title: String,
    val titleHighlight: String,
    val subtitle: String,
)
