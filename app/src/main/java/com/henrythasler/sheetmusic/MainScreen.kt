package com.henrythasler.sheetmusic

import android.graphics.RectF
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.caverock.androidsvg.RenderOptions
import com.caverock.androidsvg.SVG
import com.henrythasler.sheetmusic.ui.theme.MyApplicationTheme

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    viewModel: VerovioViewModel = VerovioViewModel()
) {
    val context = LocalContext.current
    val verovioVersion = viewModel.verovioVersion.value
    val svgData by viewModel.svgData.collectAsStateWithLifecycle()

    LaunchedEffect(true) {
        viewModel.getVerovioVersion();
        viewModel.loadAsset(context, "data")
        viewModel.renderData()
    }

    Column(
//        modifier = modifier,
//        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Verovio $verovioVersion",
            modifier = modifier
        )
//        Text(
//            text = svgData,
//            modifier = modifier
//        )
//        SmallFloatingActionButton(
//            onClick = {
//                viewModel.renderData()
//            }
//        ) {
//            Icon(Icons.Filled.Search, "")
//        }
        if(svgData.isNotEmpty()) {
            SvgImage(
                // chord-005 saved by Inkscape
//                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
//                        "<svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:inkscape=\"http://www.inkscape.org/namespaces/inkscape\" xmlns:sodipodi=\"http://sodipodi.sourceforge.net/DTD/sodipodi-0.dtd\" xmlns:svg=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" width=\"400px\" height=\"400px\" version=\"1.1\" overflow=\"visible\" id=\"r1c6o43j\" sodipodi:docname=\"chord-005.svg\" inkscape:version=\"1.3.2 (091e20e, 2023-11-25)\"><sodipodi:namedview id=\"namedview13\" pagecolor=\"#ffffff\" bordercolor=\"#000000\" borderopacity=\"0.25\" inkscape:showpageshadow=\"2\" inkscape:pageopacity=\"0.0\" inkscape:pagecheckerboard=\"0\" inkscape:deskcolor=\"#d1d1d1\" inkscape:zoom=\"2.3913615\" inkscape:cx=\"183.99561\" inkscape:cy=\"198.21345\" inkscape:window-width=\"1850\" inkscape:window-height=\"1136\" inkscape:window-x=\"70\" inkscape:window-y=\"27\" inkscape:window-maximized=\"1\" inkscape:current-layer=\"r1c6o43j\" /><desc id=\"desc1\">Engraved by Verovio 5.2.0-dev-9f6ffd2</desc><defs id=\"defs3\"><symbol id=\"E0A2-r1c6o43j\" viewBox=\"0 0 1000 1000\" overflow=\"inherit\"><path transform=\"scale(1,-1)\" d=\"M198 133c102 0 207 -45 207 -133c0 -92 -118 -133 -227 -133c-101 0 -178 46 -178 133c0 88 93 133 198 133zM293 -21c0 14 -3 29 -8 44c-7 20 -18 38 -33 54c-20 21 -43 31 -68 31l-20 -2c-15 -5 -27 -14 -36 -28c-4 -9 -6 -17 -8 -24s-3 -16 -3 -27c0 -15 3 -34 9 -57 s18 -41 34 -55c15 -15 36 -23 62 -23c4 0 10 1 18 2c19 5 32 15 40 30s13 34 13 55z\" id=\"path1\" /></symbol><symbol id=\"E050-r1c6o43j\" viewBox=\"0 0 1000 1000\" overflow=\"inherit\"><path transform=\"scale(1,-1)\" d=\"M441 -245c-23 -4 -48 -6 -76 -6c-59 0 -102 7 -130 20c-88 42 -150 93 -187 154c-26 44 -43 103 -48 176c0 6 -1 13 -1 19c0 54 15 111 45 170c29 57 65 106 110 148s96 85 153 127c-3 16 -8 46 -13 92c-4 43 -5 73 -5 89c0 117 16 172 69 257c34 54 64 82 89 82 c21 0 43 -30 69 -92s39 -115 41 -159v-15c0 -109 -21 -162 -67 -241c-13 -20 -63 -90 -98 -118c-13 -9 -25 -19 -37 -29l31 -181c8 1 18 2 28 2c58 0 102 -12 133 -35c59 -43 92 -104 98 -184c1 -7 1 -15 1 -22c0 -123 -87 -209 -181 -248c8 -57 17 -110 25 -162 c5 -31 6 -58 6 -80c0 -30 -5 -53 -14 -70c-35 -64 -88 -99 -158 -103c-5 0 -11 -1 -16 -1c-37 0 -72 10 -108 27c-50 24 -77 59 -80 105v11c0 29 7 55 20 76c18 28 45 42 79 44h6c49 0 93 -42 97 -87v-9c0 -51 -34 -86 -105 -106c17 -24 51 -36 102 -36c62 0 116 43 140 85 c9 16 13 41 13 74c0 20 -1 42 -5 67c-8 53 -18 106 -26 159zM461 939c-95 0 -135 -175 -135 -286c0 -24 2 -48 5 -71c50 39 92 82 127 128c40 53 60 100 60 140v8c-4 53 -22 81 -55 81h-2zM406 119l54 -326c73 25 110 78 110 161c0 7 0 15 -1 23c-7 95 -57 142 -151 142h-12 zM382 117c-72 -2 -128 -47 -128 -120v-7c2 -46 43 -99 75 -115c-3 -2 -7 -5 -10 -10c-70 33 -116 88 -123 172v11c0 68 44 126 88 159c23 17 49 29 78 36l-29 170c-21 -13 -52 -37 -92 -73c-50 -44 -86 -84 -109 -119c-45 -69 -67 -130 -67 -182v-13c5 -68 35 -127 93 -176 s125 -73 203 -73c25 0 50 3 75 9c-19 111 -36 221 -54 331z\" id=\"path2\" /></symbol><symbol id=\"E08A-r1c6o43j\" viewBox=\"0 0 1000 1000\" overflow=\"inherit\"><path transform=\"scale(1,-1)\" d=\"M340 179c-9 24 -56 41 -89 41c-46 0 -81 -28 -100 -58c-17 -28 -25 -78 -25 -150c0 -65 2 -111 8 -135c8 -31 18 -49 40 -67c20 -17 43 -25 70 -25c54 0 92 36 115 75c14 25 23 54 28 88h27c0 -63 -24 -105 -58 -141c-35 -38 -82 -56 -140 -56c-45 0 -83 13 -115 39 c-57 45 -101 130 -101 226c0 59 33 127 68 163c36 37 97 72 160 72c36 0 93 -21 121 -40c11 -8 23 -17 33 -30c19 -23 27 -48 27 -76c0 -51 -35 -88 -86 -88c-43 0 -76 27 -76 68c0 26 7 35 21 51c15 17 32 27 58 32c7 2 14 7 14 11z\" id=\"path3\" /></symbol></defs><style type=\"text/css\" id=\"style3\">#r1c6o43j g.page-margin {font-family:Times,serif;}#r1c6o43j g.ending, #r1c6o43j g.fing, #r1c6o43j g.reh, #r1c6o43j g.tempo {font-weight:bold;}#r1c6o43j g.dir, #r1c6o43j g.dynam, #r1c6o43j g.mNum {font-style:italic;}#r1c6o43j g.label {font-weight:normal;}#r1c6o43j path {stroke:currentColor}</style><svg class=\"definition-scale\" color=\"black\" viewBox=\"0 0 4000 4000\" version=\"1.1\" id=\"svg13\"><g class=\"page-margin\" transform=\"translate(500, 500)\" id=\"g13\"><g id=\"brks3ru\" class=\"mdiv pageMilestone\" /><g id=\"b12fj504\" class=\"score pageMilestone\" /><g id=\"svxuoxy\" class=\"system\"><g id=\"k14xtjfm\" class=\"section systemMilestone\" /><g id=\"y1bzfuy0\" class=\"measure\"><g id=\"xb3fohs\" class=\"staff\"><path d=\"M0 540 L3000 540\" stroke-width=\"13\" id=\"path4\" /><path d=\"M0 720 L3000 720\" stroke-width=\"13\" id=\"path5\" /><path d=\"M0 900 L3000 900\" stroke-width=\"13\" id=\"path6\" /><path d=\"M0 1080 L3000 1080\" stroke-width=\"13\" id=\"path7\" /><path d=\"M0 1260 L3000 1260\" stroke-width=\"13\" id=\"path8\" /><g id=\"i1q4qjgl\" class=\"clef\"><use xlink:href=\"#E050-r1c6o43j\" x=\"90\" y=\"1080\" height=\"720px\" width=\"720px\" id=\"use8\" /></g><g id=\"bdtdzk4\" class=\"keySig\" /><g id=\"xnng4ud\" class=\"meterSig\"><use xlink:href=\"#E08A-r1c6o43j\" x=\"735\" y=\"900\" height=\"720px\" width=\"720px\" id=\"use9\" /></g><g class=\"ledgerLines below\" id=\"g9\"><path d=\"M1263 1440 L1649 1440\" stroke-width=\"22\" id=\"path9\" /></g><g id=\"rr028o5\" class=\"layer\"><g id=\"jewt9ie\" class=\"chord\"><g id=\"m1l9lka8\" class=\"note\"><g class=\"notehead\" id=\"g10\"><use xlink:href=\"#E0A2-r1c6o43j\" x=\"1311\" y=\"1440\" height=\"720px\" width=\"720px\" id=\"use10\" /></g></g><g id=\"ozw2gy9\" class=\"note\"><g class=\"notehead\" id=\"g11\"><use xlink:href=\"#E0A2-r1c6o43j\" x=\"1311\" y=\"1260\" height=\"720px\" width=\"720px\" id=\"use11\" /></g></g><g id=\"w1g2rbv8\" class=\"note\"><g class=\"notehead\" id=\"g12\"><use xlink:href=\"#E0A2-r1c6o43j\" x=\"1311\" y=\"1080\" height=\"720px\" width=\"720px\" id=\"use12\" /></g></g></g></g></g><g id=\"duzq8ej\" class=\"barLine\"><path d=\"M2825 540 L2825 1260\" stroke-width=\"27\" id=\"path12\" /><path d=\"M2955 540 L2955 1260\" stroke-width=\"90\" id=\"path13\" /></g></g><g id=\"lk4al5n\" class=\"systemMilestoneEnd k14xtjfm\" /></g><g id=\"d1ctaahm\" class=\"pageMilestoneEnd b12fj504\" /><g id=\"b1g4wua4\" class=\"pageMilestoneEnd brks3ru\" /><g id=\"et4q876\" class=\"pgHead autogenerated\" /></g></svg></svg>\n",
                // chord-005
//                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
//                        "<svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" width=\"400px\" height=\"400px\" version=\"1.1\" overflow=\"visible\" id=\"r1c6o43j\"><desc>Engraved by Verovio 5.2.0-dev-9f6ffd2</desc><defs><symbol id=\"E0A2-r1c6o43j\" viewBox=\"0 0 1000 1000\" overflow=\"inherit\"><path transform=\"scale(1,-1)\" d=\"M198 133c102 0 207 -45 207 -133c0 -92 -118 -133 -227 -133c-101 0 -178 46 -178 133c0 88 93 133 198 133zM293 -21c0 14 -3 29 -8 44c-7 20 -18 38 -33 54c-20 21 -43 31 -68 31l-20 -2c-15 -5 -27 -14 -36 -28c-4 -9 -6 -17 -8 -24s-3 -16 -3 -27c0 -15 3 -34 9 -57 s18 -41 34 -55c15 -15 36 -23 62 -23c4 0 10 1 18 2c19 5 32 15 40 30s13 34 13 55z\" /></symbol><symbol id=\"E050-r1c6o43j\" viewBox=\"0 0 1000 1000\" overflow=\"inherit\"><path transform=\"scale(1,-1)\" d=\"M441 -245c-23 -4 -48 -6 -76 -6c-59 0 -102 7 -130 20c-88 42 -150 93 -187 154c-26 44 -43 103 -48 176c0 6 -1 13 -1 19c0 54 15 111 45 170c29 57 65 106 110 148s96 85 153 127c-3 16 -8 46 -13 92c-4 43 -5 73 -5 89c0 117 16 172 69 257c34 54 64 82 89 82 c21 0 43 -30 69 -92s39 -115 41 -159v-15c0 -109 -21 -162 -67 -241c-13 -20 -63 -90 -98 -118c-13 -9 -25 -19 -37 -29l31 -181c8 1 18 2 28 2c58 0 102 -12 133 -35c59 -43 92 -104 98 -184c1 -7 1 -15 1 -22c0 -123 -87 -209 -181 -248c8 -57 17 -110 25 -162 c5 -31 6 -58 6 -80c0 -30 -5 -53 -14 -70c-35 -64 -88 -99 -158 -103c-5 0 -11 -1 -16 -1c-37 0 -72 10 -108 27c-50 24 -77 59 -80 105v11c0 29 7 55 20 76c18 28 45 42 79 44h6c49 0 93 -42 97 -87v-9c0 -51 -34 -86 -105 -106c17 -24 51 -36 102 -36c62 0 116 43 140 85 c9 16 13 41 13 74c0 20 -1 42 -5 67c-8 53 -18 106 -26 159zM461 939c-95 0 -135 -175 -135 -286c0 -24 2 -48 5 -71c50 39 92 82 127 128c40 53 60 100 60 140v8c-4 53 -22 81 -55 81h-2zM406 119l54 -326c73 25 110 78 110 161c0 7 0 15 -1 23c-7 95 -57 142 -151 142h-12 zM382 117c-72 -2 -128 -47 -128 -120v-7c2 -46 43 -99 75 -115c-3 -2 -7 -5 -10 -10c-70 33 -116 88 -123 172v11c0 68 44 126 88 159c23 17 49 29 78 36l-29 170c-21 -13 -52 -37 -92 -73c-50 -44 -86 -84 -109 -119c-45 -69 -67 -130 -67 -182v-13c5 -68 35 -127 93 -176 s125 -73 203 -73c25 0 50 3 75 9c-19 111 -36 221 -54 331z\" /></symbol><symbol id=\"E08A-r1c6o43j\" viewBox=\"0 0 1000 1000\" overflow=\"inherit\"><path transform=\"scale(1,-1)\" d=\"M340 179c-9 24 -56 41 -89 41c-46 0 -81 -28 -100 -58c-17 -28 -25 -78 -25 -150c0 -65 2 -111 8 -135c8 -31 18 -49 40 -67c20 -17 43 -25 70 -25c54 0 92 36 115 75c14 25 23 54 28 88h27c0 -63 -24 -105 -58 -141c-35 -38 -82 -56 -140 -56c-45 0 -83 13 -115 39 c-57 45 -101 130 -101 226c0 59 33 127 68 163c36 37 97 72 160 72c36 0 93 -21 121 -40c11 -8 23 -17 33 -30c19 -23 27 -48 27 -76c0 -51 -35 -88 -86 -88c-43 0 -76 27 -76 68c0 26 7 35 21 51c15 17 32 27 58 32c7 2 14 7 14 11z\" /></symbol></defs><style type=\"text/css\">#r1c6o43j g.page-margin {font-family:Times,serif;}#r1c6o43j g.ending, #r1c6o43j g.fing, #r1c6o43j g.reh, #r1c6o43j g.tempo {font-weight:bold;}#r1c6o43j g.dir, #r1c6o43j g.dynam, #r1c6o43j g.mNum {font-style:italic;}#r1c6o43j g.label {font-weight:normal;}#r1c6o43j path {stroke:currentColor}</style><svg class=\"definition-scale\" color=\"black\" viewBox=\"0 0 4000 4000\"><g class=\"page-margin\" transform=\"translate(500, 500)\"><g id=\"brks3ru\" class=\"mdiv pageMilestone\" /><g id=\"b12fj504\" class=\"score pageMilestone\" /><g id=\"svxuoxy\" class=\"system\"><g id=\"k14xtjfm\" class=\"section systemMilestone\" /><g id=\"y1bzfuy0\" class=\"measure\"><g id=\"xb3fohs\" class=\"staff\"><path d=\"M0 540 L3000 540\" stroke-width=\"13\" /><path d=\"M0 720 L3000 720\" stroke-width=\"13\" /><path d=\"M0 900 L3000 900\" stroke-width=\"13\" /><path d=\"M0 1080 L3000 1080\" stroke-width=\"13\" /><path d=\"M0 1260 L3000 1260\" stroke-width=\"13\" /><g id=\"i1q4qjgl\" class=\"clef\"><use xlink:href=\"#E050-r1c6o43j\" x=\"90\" y=\"1080\" height=\"720px\" width=\"720px\" /></g><g id=\"bdtdzk4\" class=\"keySig\" /><g id=\"xnng4ud\" class=\"meterSig\"><use xlink:href=\"#E08A-r1c6o43j\" x=\"735\" y=\"900\" height=\"720px\" width=\"720px\" /></g><g class=\"ledgerLines below\"><path d=\"M1263 1440 L1649 1440\" stroke-width=\"22\" /></g><g id=\"rr028o5\" class=\"layer\"><g id=\"jewt9ie\" class=\"chord\"><g id=\"m1l9lka8\" class=\"note\"><g class=\"notehead\"><use xlink:href=\"#E0A2-r1c6o43j\" x=\"1311\" y=\"1440\" height=\"720px\" width=\"720px\" /></g></g><g id=\"ozw2gy9\" class=\"note\"><g class=\"notehead\"><use xlink:href=\"#E0A2-r1c6o43j\" x=\"1311\" y=\"1260\" height=\"720px\" width=\"720px\" /></g></g><g id=\"w1g2rbv8\" class=\"note\"><g class=\"notehead\"><use xlink:href=\"#E0A2-r1c6o43j\" x=\"1311\" y=\"1080\" height=\"720px\" width=\"720px\" /></g></g></g></g></g><g id=\"duzq8ej\" class=\"barLine\"><path d=\"M2825 540 L2825 1260\" stroke-width=\"27\" /><path d=\"M2955 540 L2955 1260\" stroke-width=\"90\" /></g></g><g id=\"lk4al5n\" class=\"systemMilestoneEnd k14xtjfm\" /></g><g id=\"d1ctaahm\" class=\"pageMilestoneEnd b12fj504\" /><g id=\"b1g4wua4\" class=\"pageMilestoneEnd brks3ru\" /><g id=\"et4q876\" class=\"pgHead autogenerated\" /></g></svg></svg>\n",
                // beam-009
//                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
//                        "<svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" width=\"400px\" height=\"400px\" version=\"1.1\" overflow=\"visible\" id=\"r117qpy8\"><desc>Engraved by Verovio 5.2.0-dev-9f6ffd2</desc><defs><symbol id=\"E0A4-r117qpy8\" viewBox=\"0 0 1000 1000\" overflow=\"inherit\"><path transform=\"scale(1,-1)\" d=\"M0 -39c0 68 73 172 200 172c66 0 114 -37 114 -95c0 -84 -106 -171 -218 -171c-64 0 -96 30 -96 94z\" /></symbol><symbol id=\"E050-r117qpy8\" viewBox=\"0 0 1000 1000\" overflow=\"inherit\"><path transform=\"scale(1,-1)\" d=\"M441 -245c-23 -4 -48 -6 -76 -6c-59 0 -102 7 -130 20c-88 42 -150 93 -187 154c-26 44 -43 103 -48 176c0 6 -1 13 -1 19c0 54 15 111 45 170c29 57 65 106 110 148s96 85 153 127c-3 16 -8 46 -13 92c-4 43 -5 73 -5 89c0 117 16 172 69 257c34 54 64 82 89 82 c21 0 43 -30 69 -92s39 -115 41 -159v-15c0 -109 -21 -162 -67 -241c-13 -20 -63 -90 -98 -118c-13 -9 -25 -19 -37 -29l31 -181c8 1 18 2 28 2c58 0 102 -12 133 -35c59 -43 92 -104 98 -184c1 -7 1 -15 1 -22c0 -123 -87 -209 -181 -248c8 -57 17 -110 25 -162 c5 -31 6 -58 6 -80c0 -30 -5 -53 -14 -70c-35 -64 -88 -99 -158 -103c-5 0 -11 -1 -16 -1c-37 0 -72 10 -108 27c-50 24 -77 59 -80 105v11c0 29 7 55 20 76c18 28 45 42 79 44h6c49 0 93 -42 97 -87v-9c0 -51 -34 -86 -105 -106c17 -24 51 -36 102 -36c62 0 116 43 140 85 c9 16 13 41 13 74c0 20 -1 42 -5 67c-8 53 -18 106 -26 159zM461 939c-95 0 -135 -175 -135 -286c0 -24 2 -48 5 -71c50 39 92 82 127 128c40 53 60 100 60 140v8c-4 53 -22 81 -55 81h-2zM406 119l54 -326c73 25 110 78 110 161c0 7 0 15 -1 23c-7 95 -57 142 -151 142h-12 zM382 117c-72 -2 -128 -47 -128 -120v-7c2 -46 43 -99 75 -115c-3 -2 -7 -5 -10 -10c-70 33 -116 88 -123 172v11c0 68 44 126 88 159c23 17 49 29 78 36l-29 170c-21 -13 -52 -37 -92 -73c-50 -44 -86 -84 -109 -119c-45 -69 -67 -130 -67 -182v-13c5 -68 35 -127 93 -176 s125 -73 203 -73c25 0 50 3 75 9c-19 111 -36 221 -54 331z\" /></symbol></defs><style type=\"text/css\">#r117qpy8 g.page-margin {font-family:Times,serif;}#r117qpy8 g.ending, #r117qpy8 g.fing, #r117qpy8 g.reh, #r117qpy8 g.tempo {font-weight:bold;}#r117qpy8 g.dir, #r117qpy8 g.dynam, #r117qpy8 g.mNum {font-style:italic;}#r117qpy8 g.label {font-weight:normal;}#r117qpy8 path {stroke:currentColor}</style><svg class=\"definition-scale\" color=\"black\" viewBox=\"0 0 4000 4000\"><g class=\"page-margin\" transform=\"translate(500, 500)\"><g id=\"bvzc44b\" class=\"mdiv pageMilestone\" /><g id=\"bh2w4ao\" class=\"score pageMilestone\" /><g id=\"f1gxwsza\" class=\"system\"><g id=\"w1i2zsyh\" class=\"section systemMilestone\" /><g id=\"kbtucpd\" class=\"measure\"><g id=\"c1sbq6jv\" class=\"staff\"><path d=\"M0 540 L3000 540\" stroke-width=\"13\" /><path d=\"M0 720 L3000 720\" stroke-width=\"13\" /><path d=\"M0 900 L3000 900\" stroke-width=\"13\" /><path d=\"M0 1080 L3000 1080\" stroke-width=\"13\" /><path d=\"M0 1260 L3000 1260\" stroke-width=\"13\" /><g id=\"o11ppae8\" class=\"clef\"><use xlink:href=\"#E050-r117qpy8\" x=\"90\" y=\"1080\" height=\"720px\" width=\"720px\" /></g><g id=\"q19llmb6\" class=\"keySig\" /><g id=\"dg34j0i\" class=\"layer\"><g id=\"xc3aj67\" class=\"beam\"><polygon stroke-opacity=\"1\" fill-opacity=\"1\" points=\"1066,540 1617,495 1617,585 1066,630\" /><g id=\"ri1utr7\" class=\"note\"><g class=\"notehead\"><use xlink:href=\"#E0A4-r117qpy8\" x=\"858\" y=\"1350\" height=\"720px\" width=\"720px\" /></g><g id=\"cotur98\" class=\"stem\"><path d=\"M1075 1322 L1075 558\" stroke-width=\"18\" /></g></g><g id=\"j43meg9\" class=\"note\"><g class=\"notehead\"><use xlink:href=\"#E0A4-r117qpy8\" x=\"1391\" y=\"1170\" height=\"720px\" width=\"720px\" /></g><g id=\"di8py5l\" class=\"stem\"><path d=\"M1608 1142 L1608 513\" stroke-width=\"18\" /></g></g></g><g id=\"mvxi659\" class=\"beam\"><polygon stroke-opacity=\"1\" fill-opacity=\"1\" points=\"2131,540 2681,495 2681,585 2131,630\" /><g id=\"o1l43nuz\" class=\"note\"><g class=\"notehead\"><use xlink:href=\"#E0A4-r117qpy8\" x=\"1923\" y=\"1260\" height=\"720px\" width=\"720px\" /></g><g id=\"yiuw4w8\" class=\"stem\"><path d=\"M2140 1232 L2140 558\" stroke-width=\"18\" /></g></g><g id=\"w4p7svq\" class=\"note\"><g class=\"notehead\"><use xlink:href=\"#E0A4-r117qpy8\" x=\"2455\" y=\"1170\" height=\"720px\" width=\"720px\" /></g><g id=\"gjvh4ar\" class=\"stem\"><path d=\"M2672 1142 L2672 513\" stroke-width=\"18\" /></g></g></g></g></g><g id=\"s11b3tix\" class=\"barLine\"><path d=\"M2987 540 L2987 1260\" stroke-width=\"27\" /></g></g><g id=\"l342esl\" class=\"systemMilestoneEnd w1i2zsyh\" /></g><g id=\"d196l3g3\" class=\"pageMilestoneEnd bh2w4ao\" /><g id=\"b86aopg\" class=\"pageMilestoneEnd bvzc44b\" /><g id=\"e1a2tzjz\" class=\"pgHead autogenerated\" /></g></svg></svg>\n",

                svgData,
                modifier = Modifier
                    .width(400.dp)
                    .height((400.dp))
//                    .fillMaxSize()
                    .border(1.dp, Color.Red)
                )
        }
        SvgImage(
            "<svg version=\"1.2\" xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 457 120\" width=\"457\" height=\"120\">\n" +
                    "\t<title>Sample.Cat</title>\n" +
                    "\t<style>\n" +
                    "\t\t.s0 { fill: #293036 } \n" +
                    "\t\t.s1 { fill: #218ae3 } \n" +
                    "\t</style>\n" +
                    "\t<path id=\"Sample.Cat\" class=\"s0\" aria-label=\"Sample.Cat\"  d=\"m138.7 68.8q0-2.2-1.7-3.6-1.6-1.4-4.1-2.5-2.4-1.2-5.4-2.4-2.8-1.3-5.4-3-2.4-1.8-4.1-4.5-1.6-2.6-1.6-6.8 0-3.6 1.2-6.1 1.2-2.7 3.3-4.5 2.3-1.7 5.3-2.5 3-0.9 6.7-0.9 4.3 0 8.1 0.8 3.7 0.7 6.2 2.1l-3 8q-1.6-0.9-4.6-1.7-3.1-0.9-6.7-0.9-3.3 0-5.2 1.4-1.7 1.3-1.7 3.6 0 2 1.6 3.5 1.7 1.4 4.1 2.5 2.5 1.2 5.4 2.5 3 1.2 5.4 3.1 2.5 1.7 4.1 4.4 1.7 2.6 1.7 6.5 0 3.9-1.3 6.8-1.3 2.8-3.7 4.7-2.3 1.9-5.6 2.8-3.3 0.9-7.3 0.9-5.3 0-9.2-1-4-1-5.9-2l3.1-8.2q0.7 0.5 1.9 1 1.3 0.5 2.8 0.9 1.6 0.4 3.2 0.7 1.8 0.3 3.6 0.3 4.3 0 6.5-1.4 2.3-1.5 2.3-4.5zm17.7-13l-2.1-6.8q2.8-1.2 6.6-1.9 3.9-0.8 8.1-0.8 3.6 0 6.1 0.9 2.4 0.8 3.8 2.5 1.5 1.6 2 3.8 0.7 2.3 0.7 5 0 3.1-0.2 6.3-0.3 3.1-0.3 6.1 0 3 0.2 5.9 0.2 2.7 1 5.3h-7.4l-1.5-4.9h-0.3q-1.4 2.2-3.9 3.8-2.5 1.6-6.4 1.6-2.4 0-4.4-0.7-2-0.8-3.4-2.1-1.4-1.4-2.1-3.3-0.8-1.8-0.8-4.2 0-3.2 1.4-5.3 1.5-2.3 4.1-3.6 2.8-1.4 6.5-1.9 3.7-0.6 8.4-0.4 0.5-3.9-0.6-5.6-1-1.7-4.7-1.7-2.7 0-5.8 0.5-3 0.6-5 1.5zm9.4 19.3q2.7 0 4.4-1.2 1.6-1.3 2.3-2.7v-4.5q-2.1-0.2-4.2-0.1-1.9 0.1-3.5 0.6-1.5 0.5-2.4 1.4-0.9 0.9-0.9 2.3 0 2 1.1 3.1 1.2 1.1 3.2 1.1zm52.4 6.9h-9.1v-19q0-4.9-0.9-6.9-0.9-2-3.8-2-2.4 0-3.9 1.3-1.4 1.3-2.2 3.3v23.3h-9.1v-35h7.1l1 4.6h0.3q1.6-2.2 4.1-3.9 2.5-1.7 6.4-1.7 3.4 0 5.5 1.4 2.1 1.4 3.3 4.6 1.6-2.8 4.1-4.4 2.6-1.6 6.1-1.6 3 0 5 0.7 2.1 0.7 3.4 2.5 1.3 1.6 1.9 4.5 0.7 2.9 0.7 7.3v21h-9.1v-19.7q0-4.1-1-6.1-0.8-2.1-3.8-2.1-2.5 0-3.9 1.3-1.4 1.3-2.1 3.6zm27.3 14v-49h6.7l1 4.2h0.3q1.8-2.6 4.3-3.9 2.5-1.2 6.1-1.2 6.6 0 9.9 4.2 3.2 4.1 3.2 13.3 0 4.5-1 8.1-1.1 3.6-3.2 6.1-2 2.6-5 3.9-2.9 1.3-6.8 1.3-2.2 0-3.6-0.3-1.4-0.3-2.8-1v14.3zm15.7-42.2q-2.7 0-4.2 1.3-1.5 1.3-2.4 4v14.5q1 0.8 2.1 1.3 1.2 0.4 3.1 0.4 3.9 0 5.9-2.7 2-2.8 2-9.2 0-4.6-1.6-7.1-1.5-2.5-4.9-2.5zm31.5-20.8v37.3q0 2.4 0.7 3.6 0.6 1.1 2 1.1 0.8 0 1.6-0.2 0.8-0.1 2-0.6l1 7.1q-1.1 0.6-3.4 1.2-2.3 0.5-4.8 0.5-4 0-6.1-1.8-2.1-1.9-2.1-6.2v-42zm36.2 40.1l3 5.9q-2.1 1.7-5.7 2.9-3.6 1.1-7.6 1.1-8.5 0-12.4-4.9-4-4.9-4-13.6 0-9.2 4.5-13.8 4.4-4.6 12.3-4.6 2.7 0 5.2 0.7 2.5 0.7 4.5 2.3 2 1.6 3.2 4.3 1.1 2.7 1.1 6.8 0 1.5-0.2 3.2-0.1 1.6-0.5 3.5h-21q0.3 4.4 2.3 6.6 2.1 2.2 6.7 2.2 2.9 0 5.1-0.8 2.3-0.9 3.5-1.8zm-10-19.8q-3.6 0-5.3 2.2-1.7 2.1-2 5.7h13q0.3-3.8-1.2-5.8-1.4-2.1-4.5-2.1zm19.5 24.1q0-2.4 1.6-3.8 1.5-1.5 4-1.5 2.7 0 4.2 1.5 1.6 1.4 1.6 3.8 0 2.5-1.6 3.9-1.5 1.5-4.2 1.5-2.5 0-4-1.5-1.6-1.4-1.6-3.9zm49.7-5.2l2 7.8q-2.2 1.6-5.7 2.3-3.5 0.7-7.1 0.7-4.4 0-8.4-1.3-3.9-1.4-6.9-4.4-3.1-3.1-4.9-7.9-1.7-4.9-1.7-11.9 0-7.2 1.9-12 2.1-4.9 5.2-7.8 3.2-3.1 7.2-4.3 3.9-1.3 7.8-1.3 4.2 0 7.1 0.5 2.9 0.6 4.8 1.4l-1.9 8.1q-1.6-0.8-3.8-1.1-2.2-0.4-5.4-0.4-5.8 0-9.3 4.1-3.5 4.2-3.5 12.8 0 3.8 0.8 7 0.8 3 2.5 5.3 1.8 2.2 4.3 3.4 2.6 1.2 5.9 1.2 3.2 0 5.4-0.6 2.1-0.6 3.7-1.6zm8.9-16.3l-2.1-6.8q2.8-1.3 6.6-2 3.9-0.8 8.1-0.8 3.6 0 6 0.9 2.5 0.9 3.9 2.5 1.5 1.6 2 3.9 0.7 2.2 0.7 5 0 3.1-0.3 6.2-0.2 3.1-0.2 6.1 0 3 0.2 5.9 0.2 2.8 1 5.3h-7.4l-1.5-4.8h-0.3q-1.4 2.2-3.9 3.8-2.5 1.5-6.4 1.5-2.5 0-4.4-0.7-2-0.8-3.4-2.1-1.4-1.4-2.1-3.2-0.8-1.9-0.8-4.2 0-3.2 1.4-5.4 1.5-2.3 4.1-3.6 2.7-1.4 6.5-1.9 3.7-0.5 8.4-0.3 0.4-3.9-0.6-5.6-1.1-1.8-4.7-1.8-2.7 0-5.8 0.6-3 0.6-5 1.5zm9.4 19.2q2.7 0 4.3-1.2 1.7-1.2 2.4-2.6v-4.6q-2.1-0.2-4.2-0.1-1.9 0.2-3.5 0.7-1.5 0.5-2.4 1.4-0.9 0.9-0.9 2.3 0 1.9 1.1 3.1 1.2 1 3.2 1zm19.7-20.4v-7.7h4.9v-6.5l9.1-2.6v9.1h8.5v7.7h-8.5v13.5q0 3.6 0.7 5.2 0.7 1.6 2.8 1.6 1.4 0 2.4-0.3 1-0.3 2.3-0.8l1.6 7q-1.9 0.9-4.4 1.5-2.6 0.6-5.1 0.6-4.9 0-7.2-2.4-2.2-2.5-2.2-8.2v-17.7z\"/>\n" +
                    "\t<g>\n" +
                    "\t\t<path fill-rule=\"evenodd\" class=\"s1\" d=\"m92 64.3c0 20.3 0 30.5-6.3 36.9-6.4 6.3-16.6 6.3-37 6.3-20.3 0-30.5 0-36.9-6.3-6.3-6.4-6.3-16.6-6.3-36.9 0-20.4 0-30.6 6.3-36.9-1.1-6.8-1.4-13.5 1.4-14.6 5.5-2.4 18.6 0.6 26.3 8.2q4.2 0 9.2 0 5.5 0 10.1 0c7.7-7.5 20.3-10.5 25.9-8.2 2.8 1.2 2.5 8.1 1.4 15 5.9 6.4 5.9 16.6 5.9 36.5zm-55-18.9l-0.4 0.4-9.7 9.5-0.3 0.2c-1.8 1.8-3 3-3.9 4.2q-1.5 2.1-2.3 4.6-0.8 2.5-0.8 5.1 0 2.6 0.8 5.1 0.8 2.5 2.3 4.6c0.9 1.3 2.1 2.5 3.9 4.3l0.3 0.2c0.6 0.6 1.4 0.9 2.3 0.9 0.9 0 1.7-0.3 2.3-0.9 0.6-0.7 0.9-1.5 0.9-2.4 0-0.8-0.4-1.7-1-2.3-2.2-2.1-2.9-2.8-3.4-3.5q-1-1.3-1.5-2.9-0.5-1.5-0.5-3.1 0-1.6 0.5-3.1 0.5-1.5 1.5-2.8c0.5-0.7 1.2-1.5 3.4-3.6l9.7-9.4c3.9-3.8 5.3-5.1 6.7-5.8q1.1-0.6 2.4-0.9 1.2-0.3 2.5-0.3 1.3 0 2.6 0.3 1.2 0.3 2.4 0.9c1.4 0.7 2.8 2 6.7 5.8 3.8 3.8 5.2 5.1 5.9 6.5q0.6 1.1 0.9 2.3 0.3 1.2 0.3 2.4 0 1.2-0.3 2.4-0.3 1.2-0.9 2.3c-0.7 1.4-2.1 2.7-5.9 6.5l-9.6 9.3c-1 0.9-1.2 1.2-1.5 1.4q-0.7 0.5-1.5 0.8-0.8 0.2-1.7 0.2-0.8 0-1.6-0.2-0.9-0.3-1.5-0.8c-0.3-0.2-0.6-0.4-1.5-1.4-1-1-1.3-1.3-1.5-1.5q-0.5-0.6-0.8-1.4-0.2-0.7-0.2-1.6 0-0.8 0.2-1.5 0.3-0.8 0.8-1.4c0.2-0.3 0.5-0.5 1.5-1.5l7.3-7.1c0.6-0.7 0.9-1.5 1-2.3 0-0.9-0.4-1.7-1-2.3-0.6-0.6-1.4-1-2.2-1-0.9 0-1.7 0.3-2.3 0.9l-7.4 7.2-0.1 0.1c-0.8 0.8-1.4 1.3-1.9 1.9q-1.2 1.5-1.8 3.3-0.6 1.8-0.6 3.7 0 1.9 0.6 3.7 0.6 1.8 1.8 3.3c0.5 0.6 1.1 1.2 1.9 2l0.1 0.1 0.2 0.2q0.2 0.2 0.5 0.4 0.2 0.3 0.4 0.5 0.3 0.2 0.5 0.5 0.3 0.2 0.6 0.4 1.5 1.1 3.3 1.7 1.8 0.6 3.7 0.6 1.9 0 3.8-0.6 1.8-0.6 3.3-1.7c0.6-0.5 1.2-1.1 2-1.8l0.2-0.2 9.5-9.2 0.4-0.5c3.3-3.2 5.5-5.3 6.7-7.7q1-1.8 1.5-3.7 0.4-2 0.4-4 0-2-0.4-4-0.5-1.9-1.5-3.7c-1.2-2.4-3.4-4.5-6.7-7.7l-0.4-0.5-0.5-0.4c-3.2-3.2-5.4-5.3-7.9-6.5q-1.8-0.9-3.8-1.4-1.9-0.4-4-0.4-2 0-4 0.4-2 0.5-3.8 1.4c-2.4 1.2-4.6 3.3-7.9 6.5z\"/>\n" +
                    "\t</g>\n" +
                    "</svg>",
            modifier = Modifier
//                .fillMaxSize()
                .width(400.dp)
                .height((200.dp))
                .border(1.dp, Color.Green)
        )
//        AsyncImage(
//            model = ImageRequest.Builder(context = LocalContext.current)
//                .data(ByteBuffer.wrap(svgData.toByteArray()))
//                .decoderFactory(SvgDecoder.Factory())
//                .build(),
//            placeholder = painterResource(R.drawable.baseline_image_search_24),
//            contentDescription = stringResource(R.string.description),
//            contentScale = ContentScale.FillWidth,
//            modifier = Modifier.fillMaxSize().border(1.dp, Color.Green)
//        )

    }
}

/**
 * A composable function that renders SVG content using AndroidSVG library.
 *
 * @param svgString The SVG content as a string
 * @param contentDescription Description for accessibility
 * @param modifier Modifier for the composable
 * @param contentScale How the SVG should be scaled within its bounds
 */
@Composable
fun SvgImage(
    svgString: String,
    contentDescription: String? = null,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit
) {
    // Parse SVG in a remember block to avoid reprocessing on recomposition
    val svg = remember(svgString) {
        try {
            SVG.getFromString(svgString)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Only proceed if SVG was parsed successfully
    if (svg != null) {
        val density = LocalDensity.current.density

        // Get the document dimensions, defaulting to 100dp if not specified
        val svgWidth = if (svg.documentWidth != -1f) svg.documentWidth else 100f * density
        val svgHeight = if (svg.documentHeight != -1f) svg.documentHeight else 100f * density

        Canvas(
            modifier = modifier
                .clipToBounds() // Clip to ensure SVG stays within bounds
        ) {
            drawIntoCanvas { canvas ->
                // Calculate scaling based on ContentScale and available size
                val contentWidth = size.width
                val contentHeight = size.height

                // Calculate scaling factors for width and height
                val scaleX = contentWidth / svgWidth
                val scaleY = contentHeight / svgHeight

                // Calculate the scale factor based on ContentScale type
                val scale = when (contentScale) {
                    ContentScale.Fit -> minOf(scaleX, scaleY)
                    ContentScale.FillWidth -> scaleX
                    ContentScale.FillHeight -> scaleY
                    ContentScale.FillBounds -> 1f // Will use the viewBox transforms
                    ContentScale.Crop -> maxOf(scaleX, scaleY)
                    else -> minOf(scaleX, scaleY) // Default to Fit for other cases
                }

                // Calculate the centered position
                val scaledWidth = svgWidth * scale
                val scaledHeight = svgHeight * scale
                val left = (contentWidth - scaledWidth) / 2f
                val top = (contentHeight - scaledHeight) / 2f

                // Save the canvas state
                canvas.nativeCanvas.save()

                // Apply scaling and translation
                canvas.nativeCanvas.translate(left, top)
                canvas.nativeCanvas.scale(scale, scale)

                // Create rendering options
//                val renderOptions = RenderOptions()

                // Apply tint if specified
//                tintColor?.let {
//                    renderOptions.css("svg { fill: #${it.toArgb().toUInt().toString(16).padStart(8, '0')} }")
//                }

                // Define the viewport for rendering
//                val viewPort = RectF(0f, 0f, svgWidth, svgHeight)

                // Render the SVG to the canvas
                svg.renderToCanvas(canvas.nativeCanvas)

                // Restore the canvas state
                canvas.nativeCanvas.restore()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme {
        MainScreen()
    }
}
