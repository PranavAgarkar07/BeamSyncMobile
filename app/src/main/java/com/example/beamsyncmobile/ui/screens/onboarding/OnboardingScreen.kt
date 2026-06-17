package com.example.beamsyncmobile.ui.screens.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import com.example.beamsyncmobile.R
import com.example.beamsyncmobile.ui.components.BeamsyncButton
import com.example.beamsyncmobile.ui.components.BeamsyncButtonSize
import com.example.beamsyncmobile.ui.components.BeamsyncButtonVariant
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
            .background(MaterialTheme.colorScheme.background),
    ) {
        RadialGlowBackground(page = visiblePage)

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
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium,
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
private fun RadialGlowBackground(page: Int) {
    val glowColors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
    )
    val glowColor = glowColors[page.coerceIn(0, glowColors.size - 1)]

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    glowColor.copy(alpha = 0.12f),
                    glowColor.copy(alpha = 0.04f),
                    Color.Transparent,
                ),
                center = Offset(size.width / 2f, size.height * 0.28f),
                radius = size.width * 0.75f,
            ),
            radius = size.width * 0.75f,
            center = Offset(size.width / 2f, size.height * 0.28f),
        )
    }
}

@Composable
private fun PageContent(page: Int, isVisible: Boolean, pageOffset: Float) {
    val pages = listOf(
        OnboardingPage(
            icon = {
                Box(
                    modifier = Modifier
                        .size(148.dp)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    MaterialTheme.colorScheme.surface,
                                ),
                            ),
                            MaterialTheme.shapes.medium,
                        )
                        .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.medium),
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
            accentColor = MaterialTheme.colorScheme.primary,
        ),
        OnboardingPage(
            icon = {
                GradientIconBox(
                    gradientStart = MaterialTheme.colorScheme.primary,
                    gradientEnd = MaterialTheme.colorScheme.tertiary,
                ) {
                    Icon(
                        imageVector = Icons.Filled.CastConnected,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(72.dp),
                    )
                }
            },
            title = "SAME",
            titleHighlight = "NETWORK",
            subtitle = "Connect your phone to the same WiFi network as your desktop computer. They need to see each other.",
            accentColor = MaterialTheme.colorScheme.primary,
        ),
        OnboardingPage(
            icon = {
                GradientIconBox(
                    gradientStart = MaterialTheme.colorScheme.secondary,
                    gradientEnd = MaterialTheme.colorScheme.primary,
                ) {
                    Icon(
                        imageVector = Icons.Filled.NetworkCheck,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(72.dp),
                    )
                }
            },
            title = "5GHz",
            titleHighlight = "HOTSPOT",
            subtitle = "If using a mobile hotspot, switch the band to 5GHz in your phone's settings for maximum transfer speeds.",
            accentColor = MaterialTheme.colorScheme.secondary,
        ),
    )

    val data = pages[page]
    val pageProgress = (1f - pageOffset.absoluteValue).coerceIn(0f, 1f)

    val enterScale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.88f,
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
        animationSpec = tween(400, delayMillis = 120),
        label = "titleAlpha",
    )
    val subtitleAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(400, delayMillis = 260),
        label = "subtitleAlpha",
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                alpha = lerp(0.65f, 1f, pageProgress)
                cameraDistance = 12f * density
            }
            .padding(horizontal = BeamsyncSpacing.space8)
            .padding(top = 100.dp, bottom = 160.dp),
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
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                letterSpacing = 3.sp,
                modifier = Modifier.alpha(titleAlpha),
            )

        Spacer(Modifier.height(4.dp))

            Text(
                text = data.titleHighlight,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.displaySmall,
                textAlign = TextAlign.Center,
                letterSpacing = (-1).sp,
                modifier = Modifier.alpha(titleAlpha),
            )

        Spacer(Modifier.height(BeamsyncSpacing.space4))

            Text(
                text = data.subtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(subtitleAlpha),
            )
    }
}

@Composable
private fun GradientIconBox(
    gradientStart: Color,
    gradientEnd: Color,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(148.dp)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        gradientStart.copy(alpha = 0.15f),
                        gradientEnd.copy(alpha = 0.08f),
                    ),
                ),
                MaterialTheme.shapes.medium,
            )
            .border(1.dp, gradientStart.copy(alpha = 0.3f), MaterialTheme.shapes.medium),
        contentAlignment = Alignment.Center,
    ) {
        content()
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
                targetValue = if (isSelected) 32f else 8f,
                animationSpec = tween(300),
                label = "dotWidth",
            )
            val animatedAlpha by animateFloatAsState(
                targetValue = if (isSelected) 1f else 0.25f,
                animationSpec = tween(300),
                label = "dotAlpha",
            )
            Box(
                modifier = Modifier
                    .width(animatedWidth.dp)
                    .height(3.dp)
                    .alpha(animatedAlpha)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outlineVariant,
                        MaterialTheme.shapes.medium,
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
    val accentColor: Color,
)
