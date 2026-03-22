package com.example.smartwaste.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput

data class DisposalGuide(
    val id: String,
    val title: String,
    val categories: String,
    val description: String
)


val guideList = listOf(
    DisposalGuide("food", "Food Waste", "Organics, Compost", "Learn how to properly seperate food scraps..."),
    DisposalGuide("paper", "Paper Product", "Recyclables", "Used papers stacking up? Not sure what to do with it? Find out more... "),
    DisposalGuide("plastic", "Plastics", "Recyclables", "Do you know not all plastics are equal? Find out more here..."),
    DisposalGuide("metal", "Metals", "Recyclables", "Aluminum cans and tin foil should be rinsed and look out for sharp edges...")
)

@Composable
fun GuidesListScreen(onGuideClick: (String) -> Unit) {
    var verticalTranslation by remember { mutableStateOf(0f)}
    val animatedTranslation by animateFloatAsState(
        targetValue = verticalTranslation,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    val stretchScale = 1f + (animatedTranslation.coerceIn(0f, 500f)/200f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit){
                detectVerticalDragGestures(
                    onVerticalDrag = {_, dragAmount ->
                        verticalTranslation += dragAmount*0.5f
                    },
                    onDragEnd = {
                        verticalTranslation = 0f
                    }
                )
            }
    ) {

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(guideList) { guide ->
                GuideCard(
                    guide = guide,
                    onClick = { onGuideClick(guide.id) },
                    graphicsModifier = Modifier.graphicsLayer {
                        translationY = animatedTranslation * 0.1f
                        scaleY = stretchScale
                    }
                )
            }
        }
    }
}

@Composable
fun GuideCard(
    guide: DisposalGuide,
    onClick: () -> Unit,
    graphicsModifier: Modifier = Modifier){
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .then(graphicsModifier)
            .clickable { onClick()},
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(110.dp)
                    .background(Color.DarkGray),
                contentAlignment = Alignment.Center
            ){
                Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(48.dp))
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = guide.title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = "Types: ${guide.categories}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                }
                Text(
                    text = guide.description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun GuideDetailScreen(guideId:String) {
    val guide = guideList.find {it.id == guideId}
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ){
        if (guide != null) {
            Text(text = guide.title, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Applies to: ${guide.categories}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Divider(modifier = Modifier.padding(vertical = 16.dp))

            when(guide.id){
                "plastic" -> PlasticGuideContent()
                "food" -> FoodGuideContent()
                "paper" -> PaperGuideContent()
                "metal" -> MetalGuideContent()
                else -> Text("Error guide content - You are not supposed to end up here")
            }
            Spacer(modifier = Modifier.height(40.dp)) //This is just in case the words go to the very bottom of the screen
        } else {
            Text("The guide is not found")
        }
    }
}

@Composable
fun PlasticGuideContent(){
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Do you know that not all plastics can be recycled?" +
            "Plastics are categorised into 5 categories, Type 1 - 5. " +
            "However, Type 3 plastics made out of Polyvinyl Chloride, also known as PVC are non-recyclable. "+
            "Common type of type 3 plastics include pipes, cling wrap and credit cards. "+
            "To recycle responsibly you can follow the following steps.",
            fontSize = 16.sp, lineHeight = 24.sp
        )
        Text(
            text = "Step 1:",
            fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary
        )
        Text(
            text = "If the plastic item can be given a second chance, reuse or donate it!",
            fontSize = 16.sp, lineHeight = 24.sp
        )
        Text(
            text = "Step 2: ",
            fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary
        )
        Text(
            text = "Check the type of plastic. The type of plastic can be usually found at the bottom of the product with a triangle logo. If unclear, check the full list below",
            fontSize = 16.sp, lineHeight = 24.sp
        )
        Text(
            text = "Step 3:",
            fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary
        )
        Text(
            text = "Make sure the contents of the item is empty and give it a rinse before recycling",
            fontSize = 16.sp, lineHeight = 24.sp
        )
        Text(
            text = "Plastics that can or cannot be recycled",
            fontSize = 26.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary
        )
        Text(
            text = "Can be recycled",
            fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32)
        )
        Text(
            text = "Plastic Bottles \nPlastic Container \nShampoo Bottle \nBodywash Bottle \nFacial Cleanser Bottle \nDetergent Bottle",
            fontSize = 16.sp, lineHeight = 24.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Cannot be recycled",
            fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC62828)
        )
        Text(
            text = "Polystyrene Foam Product \nStyrofoam \nPlastic disposables which includes plastic cutlery and crockery \nPlastic packaging with foil \nOxo-Degradable bag \nCassette and video tapes",
            fontSize = 16.sp, lineHeight = 24.sp
        )
    }
}

@Composable
fun FoodGuideContent(){
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "bla bla bla...",
            fontSize = 16.sp, lineHeight = 24.sp
        )
        Text(
            text = "Step 1: ",
            fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary
        )
        Text(
            text = "content",
            fontSize = 16.sp, lineHeight = 24.sp
        )
        Text(
            text = "Step 2: bla bla bla",
            fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary
        )
    }
}

@Composable
fun PaperGuideContent(){
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Flyers from property agents choking up your doors? Throwing away your "+
            "egg trays directly into the general disposal bin? These can be recycled! " +
            "Follow the following steps on how you can dispose your paper products in a responsible manner",
            fontSize = 16.sp, lineHeight = 24.sp
        )
        Text(
            text = "Step 1: ",
            fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary
        )
        Text(
            text = "Check if these paper products can be repurposed. If they are books, donate it. If you have papers with an empty page on the other side, use it for rough work.",
            fontSize = 16.sp, lineHeight = 24.sp
        )
        Text(
            text = "Step 2:",
            fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary
        )
        Text(
            text = "Make sure the items are not contaminated or sanitary products. These are not accepted!",
            fontSize = 16.sp, lineHeight = 24.sp
        )
        Text(
            text = "Step 3:",
            fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary
        )
        Text(
            text = "Give the item a rinse for products with waterproof lining, such as milk cartons",
            fontSize = 16.sp, lineHeight = 24.sp
        )
    }
}

@Composable
fun MetalGuideContent(){
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "bla bla bla...",
            fontSize = 16.sp, lineHeight = 24.sp
        )
        Text(
            text = "Step 1: ",
            fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary
        )
        Text(
            text = "content",
            fontSize = 16.sp, lineHeight = 24.sp
        )
        Text(
            text = "Step 2: bla bla bla",
            fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary
        )
    }
}

