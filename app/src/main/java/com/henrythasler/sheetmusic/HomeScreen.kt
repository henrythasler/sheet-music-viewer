package com.henrythasler.sheetmusic

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.carousel.HorizontalUncontainedCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview(
    showSystemUi = true,
    backgroundColor = 0xFFFFFFFF,
    showBackground = true,
)
@Composable
fun HomeScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToBrowser: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToNotation: (fileName: String) -> Unit = {},
    verovioVersion: String? = null,
) {
    Scaffold(
        topBar = {
            HomeTopNavigationBar(
                verovioVersion = verovioVersion,
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(50.dp))
            Column(
                modifier = Modifier
                    .width(300.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.verovio_fadded_50),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth(),
                )
                Text(
                    text = "Verovio is a fast, portable and lightweight library for engraving Music Encoding Initiative (MEI) digital scores into SVG images.",
                    style = TextStyle.Default.copy(
                        lineBreak = LineBreak.Paragraph.copy(strategy = LineBreak.Strategy.Balanced)
                    ),
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(thickness = 2.dp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Examples Gallery",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            FavouritesRow(onNavigateToNotation)
//        CarouselExample()

            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { onNavigateToBrowser() }) {
                Text("Browse all examples")
            }
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(thickness = 2.dp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopNavigationBar(
    verovioVersion: String?,
) {
    TopAppBar(
//        modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing),
        colors = topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
        title = {
            Column(
                modifier = Modifier.windowInsetsPadding(WindowInsets.displayCutout),
            ) {
                Text(
                    text = "Verovio Demo",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = if (verovioVersion != null) "using Verovio $verovioVersion" else "",
                    style = MaterialTheme.typography.titleSmall
                )
            }
        },
        actions = {
            HomeActionMenu()
        },
    )
}

@Composable
fun HomeActionMenu() {
    var expanded by remember { mutableStateOf(false) }
    val openAlertDialog = remember { mutableStateOf(false) }

    IconButton(onClick = { expanded = !expanded }) {
        Icon(Icons.Default.MoreVert, contentDescription = "More options")
    }
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false }
    ) {
        DropdownMenuItem(
            text = { Text("About") },
            leadingIcon = { Icon(Icons.Outlined.Info, contentDescription = null) },
            onClick = {
                expanded = false
                openAlertDialog.value = true
            }
        )
    }

    if (openAlertDialog.value) {
        AboutDialog(openAlertDialog)
    }
}

@Composable
fun FavouritesRow(
    onNavigateToNotation: (fileName: String) -> Unit = {},
    ) {
    data class MeiItem(
        val path: String,
        val name: String
    )

    val favourites = remember {
        listOf(
            MeiItem("mei/tempo/tempo-003.mei", "tempo-003.mei"),
            MeiItem("mei/chord/chord-005.mei", "chord-005.mei"),
            MeiItem("mei/lyric/lyric-004.mei", "lyric-004.mei"),
            MeiItem("mei/arpeg/arpeg-001.mei", "arpeg-001.mei"),
            MeiItem("mei/Costeley_Je_vois_de_glissantes_eaux.mei", "Costeley_Je_vois_de_glissantes_eaux.mei"),
        )
    }

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        favourites.forEach { item ->
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .size(128.dp)
                        .background(Color.White)
                        .clickable {
                            onNavigateToNotation(item.path)
                        }
                ) {
                    Image(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .size(96.dp),
                        painter = painterResource(id = R.drawable.mei_logo_simple_light),
                        contentDescription = item.path,
//                        contentScale = ContentScale.Crop
                    )
                    Text(
                        modifier = Modifier
                            .align(Alignment.BottomCenter),
                        text = item.name,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarouselExample() {
    data class CarouselItem(
        val id: Int,
        @DrawableRes val imageResId: Int,
        val contentDescription: String
    )

    val carouselItems = remember {
        listOf(
            CarouselItem(0, R.drawable.baseline_image_search_24, "cupcake"),
            CarouselItem(1, R.drawable.outline_folder_24, "donut"),
            CarouselItem(2, R.drawable.verovio_logo_big, "eclair"),
            CarouselItem(3, R.drawable.mei_logo_simple_light, "froyo"),
            CarouselItem(4, R.drawable.baseline_arrow_back_ios_24, "gingerbread"),
        )
    }

    HorizontalUncontainedCarousel (
        state = rememberCarouselState { carouselItems.count() },
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .wrapContentHeight()
            .padding(top = 16.dp, bottom = 16.dp),
        itemWidth = 186.dp,
        itemSpacing = 8.dp,
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) { i ->
        val item = carouselItems[i]
        Image(
            modifier = Modifier
                .height(205.dp)
                .maskClip(MaterialTheme.shapes.extraLarge),
            painter = painterResource(id = item.imageResId),
            contentDescription = item.contentDescription,
            contentScale = ContentScale.Crop
        )
    }
}