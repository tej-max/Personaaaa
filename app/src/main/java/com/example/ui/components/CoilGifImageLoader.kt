package com.example.ui.components

import android.content.Context
import android.os.Build
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder

object CoilGifImageLoader {
    private var loader: ImageLoader? = null

    fun get(context: Context): ImageLoader {
        return loader ?: synchronized(this) {
            val created = ImageLoader.Builder(context.applicationContext)
                .components {
                    if (Build.VERSION.SDK_INT >= 28) {
                        add(ImageDecoderDecoder.Factory())
                    } else {
                        add(GifDecoder.Factory())
                    }
                }
                .crossfade(true)
                .build()
            loader = created
            created
        }
    }
}
