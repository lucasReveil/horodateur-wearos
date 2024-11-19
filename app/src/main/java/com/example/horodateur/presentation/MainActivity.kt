/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter to find the
 * most up to date changes to the libraries and their usages.
 */

package com.example.horodateur.presentation

import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.res.imageResource

import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.tooling.preview.devices.WearDevices
import com.example.horodateur.R
import com.example.horodateur.presentation.theme.HorodateurTheme
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HorodateurTheme { // Votre thème personnalisé
                EcranHorodateur() // Appelle le composable principal
            }
        }
    }
}
@Composable
fun EcranHorodateur(){
    val centre = remember { mutableStateOf(Offset.Zero) }
    val angleAiguilleHeure = remember { mutableStateOf(30f) } // sur le 5
    val angleAiguilleMinute = remember { mutableStateOf(60f) } // sur le 4
    val backgroundImage = ImageBitmap.imageResource(id = R.drawable.malozbeu)
    Box(modifier=Modifier.fillMaxSize(), contentAlignment=Alignment.Center) {
        Canvas(modifier=Modifier.fillMaxSize()
            .pointerInput(Unit){
                detectDragGestures { change, _ ->
                    val touchPoint = change.position
                    val angle = calculAngle(centre.value,touchPoint)
                    if(isNearHourHand(touchPoint,centre.value,angleAiguilleHeure.value)){
                        angleAiguilleHeure.value = angle
                    }
                    else if(isNearMinuteHand(touchPoint,centre.value,angleAiguilleMinute.value)){
                        angleAiguilleMinute.value=angle
                    }
                }
            }) {
            // on cherche la valeur de scale de l'image
            val scaleX=size.width/(backgroundImage.width*1.7f)
            val scaleY = size.height / (backgroundImage.height*1.7f)
            val scale = minOf(scaleX,scaleY) // pour eviter de deformer l'image
            // Calculer les décalages pour centrer l'image
            val offsetX = (0 - backgroundImage.width * scale)-size.width
            val offsetY = (0 - backgroundImage.height * scale)-size.height

            withTransform({
                scale(scale,scale)
            }) {
                drawImage(image = backgroundImage,topLeft = Offset(offsetX, offsetY))
            }
            //drawImage(image = backgroundImage,topLeft = Offset(offsetX, offsetY))
            centre.value= Offset(size.width/2,size.height/2+10)
            drawClockNumbers(centre.value,size.minDimension/2)
            drawHand(centre.value,size.minDimension/4,angleAiguilleHeure.value, Color.Black,12f)
            drawHand(centre.value,size.minDimension/3,angleAiguilleMinute.value, Color.Blue,8f)
        }
    }
}
fun DrawScope.drawClockNumbers(centre:Offset,rad:Float){
    val nombres = listOf("12","1","2","3","5","4","6","9","7","8","10","11") //selon la norme Malozbeuzienne No 54
    val textPaint = Paint().asFrameworkPaint().apply{
        color = android.graphics.Color.BLACK
        textSize=40f
        textAlign= android.graphics.Paint.Align.CENTER
    }
    for(i in nombres.indices){
        val angle = Math.toRadians((i*30 -90).toDouble())
        val x = centre.x + rad * cos(angle).toFloat() *0.85f
        val y = centre.y + rad * sin(angle).toFloat() * 0.85f
        drawContext.canvas.nativeCanvas.drawText(nombres[i],x,y,textPaint)
    }
}


fun DrawScope.drawHand(centre:Offset,longueur:Float,angle:Float,color:Color,largeur:Float){
    val xFin = centre.x+ longueur* cos(Math.toRadians(angle.toDouble())).toFloat()
    val yFin = centre.y+ longueur*sin(Math.toRadians(angle.toDouble())).toFloat()
    drawLine(color=color,start=centre,end=Offset(xFin,yFin), strokeWidth = largeur,cap= StrokeCap.Round)
}
fun calculAngle(centre:Offset,touch:Offset):Float{
    val deltaX = touch.x -centre.x
    val deltaY = touch.y-centre.y
    val rad = atan2(deltaY,deltaX)
    return Math.toDegrees(rad.toDouble()).toFloat().let{
        if(it<0) it +360 else it
    }
}

//fonction pour detecter si le toucher est proche de l'aiguille des heures
fun isNearHourHand(touch:Offset,centre:Offset,angleHeure:Float):Boolean{
    val finAiguille = Offset(
        centre.x + 100 * cos(Math.toRadians(angleHeure.toDouble())).toFloat(),
        centre.y + 100 * sin(Math.toRadians(angleHeure.toDouble())).toFloat()
    )
    return(touch-finAiguille).getDistance()<50f
}
//pareil mais pour l'aiguille des minutes
fun isNearMinuteHand(touch:Offset,centre:Offset,angleMinute:Float):Boolean{
    val finAiguille = Offset(
        centre.x + 100 * cos(Math.toRadians(angleMinute.toDouble())).toFloat(),
        centre.y + 100 * sin(Math.toRadians(angleMinute.toDouble())).toFloat()
    )
    return(touch-finAiguille).getDistance()<50f
}
