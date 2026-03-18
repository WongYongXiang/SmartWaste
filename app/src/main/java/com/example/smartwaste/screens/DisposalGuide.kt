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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.graphicsLayer


data class DisposalGuide(
    val id: String,
    val title: String,
    val categories: String,
    val description: String
)


val guideList = listOf(
    DisposalGuide("food", "Food Waste", "Organics, Compost", "Learn how to properly seperate food scraps..."),
    DisposalGuide("paper", "Paper Product", "Recyclables", "Ensure that boxes are flattened and paper are dry..."),
    DisposalGuide("plastic", "Plastics", "Recyclables", "Rinse plastic and check the type of plastic. Not all plastics are accepted..."),
    DisposalGuide("metal", "Metals", "Recyclables", "Aluminum cans and tin foil should be rinsed and look out for sharp edges...")
)

@Composable
fun GuidesListScreen(onGuideClick: (String) -> Unit) {
    val lazyListState = rememberLazyListState()
    val isAtTop = !lazyListState.canScrollBackward
    val isAtBottom = !lazyListState.canScrollForward
    LazyColumn(
        state = lazyListState,
        modifier = Modifier
          .fillMaxSize()
          .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ){
        items(guideList) { guide ->
            val scale by animateFloatAsState(
                targetValue = if (isAtTop || isAtBottom) 1.02f else 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
            GuideCard(
                guide = guide,
                onClick = { onGuideClick(guide.id)},
                graphicsModifier = Modifier.graphicsLayer{
                    scaleX = scale
                    scaleY = scale
                })
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

