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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
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
    DisposalGuide("metal", "Metals", "Recyclables", "Aluminum cans and tin foil should be rinsed and look out for sharp edges..."),
    DisposalGuide("glass", "Glass", "Recyclables", "Glass never quits. Recycle to bring new life to them...")
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
            .background(Color(0xFFFAFCFA))
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
            item{
                Text(
                    text = "Disposal Guide",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1B5E20),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
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
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(110.dp)
                    .background(Color(0xFFE8F5E9)),
                contentAlignment = Alignment.Center
            ){
                Icon(
                    Icons.Default.DeleteSweep,
                    contentDescription = null,
                    tint = Color(0xFF2E7D32),
                    modifier = Modifier.size(48.dp))
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = guide.title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = "Types: ${guide.categories}", fontSize = 12.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Medium)
                }
                Text(
                    text = guide.description,
                    fontSize = 14.sp,
                    color = Color.DarkGray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun GuideDetailScreen(guideId:String) {
    val guide = guideList.find {it.id == guideId}
    if (guide != null) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFAFCFA))
                .padding(20.dp)
        ){
            item{
                Text(text = guide.title, fontSize = 34.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1B5E20))
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Applies to: ${guide.categories}", fontSize = 16.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 16.dp),
                    color = Color(0xFFE8F5E9),
                    thickness = 2.dp
                )
                when(guide.id){
                    "plastic" -> PlasticGuideContent()
                    "food" -> FoodGuideContent()
                    "paper" -> PaperGuideContent()
                    "metal" -> MetalGuideContent()
                    "glass" -> GlassGuideContent()
                    else -> Text("Error guide content - You are not supposed to end up here")
                }
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ){Text("The guide is not found")}
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
            fontSize = 16.sp, lineHeight = 24.sp, color = Color.DarkGray
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text("How to Recycle", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))

        StepItem("1","If the plastic item can be given a second chance, reuse or donate it!")
        StepItem("2","Check the type of plastic. The type of plastic can be usually found at the bottom of the product with a triangle logo. If unclear, check the full list below")
        StepItem("1","Make sure the contents of the item is empty and give it a rinse before recycling")

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "What can or cannot be recycled",
            fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20)
        )

        InfoCard(
            title = "Can be recycled",
            text = "Plastic Bottles \nPlastic Container \nShampoo Bottle \nBodywash Bottle \nFacial Cleanser Bottle \nDetergent Bottle",
            isPositive = true
        )
        InfoCard(
            title = "Cannot be recycled",
            text = "Polystyrene Foam Product \nStyrofoam \nPlastic disposables which includes plastic cutlery and crockery \nPlastic packaging with foil \nOxo-Degradable bag \nCassette and video tapes",
            isPositive = false
        )
    }
}

@Composable
fun FoodGuideContent(){
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Properly separating your food waste from recyclables help to reduce greenhouse gas emissions",
            fontSize = 16.sp, lineHeight = 24.sp, color = Color.DarkGray
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text("How to Recycle", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))

        StepItem("1","To add")
        StepItem("2","To add")
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "What can or cannot be recycled",
            fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20)
        )

        InfoCard(
            title = "Can be recycled",
            text = "bla bla",
            isPositive = true
        )
        InfoCard(
            title = "Cannot be recycled",
            text = "bla bla bla",
            isPositive = false
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
            fontSize = 16.sp, lineHeight = 24.sp, color = Color.DarkGray
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text("How to Recycle", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))

        StepItem("1","Check if these paper products can be repurposed. If they are books, donate it. If you have papers with an empty page on the other side, use it for rough work.")
        StepItem("2","Make sure the items are not contaminated or sanitary products. These are not accepted!")
        StepItem("3","Give the item a rinse for products with waterproof lining, such as milk cartons")

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "What can or cannot be recycled",
            fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20)
        )

        InfoCard(
            title = "Can be recycled",
            text = "bla bla",
            isPositive = true
        )
        InfoCard(
            title = "Cannot be recycled",
            text = "bla bla bla",
            isPositive = false
        )

    }
}

@Composable
fun MetalGuideContent(){
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Some metals can be recycled! Don't be so quick to dispose them!",
            fontSize = 16.sp, lineHeight = 24.sp, color = Color.DarkGray
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text("How to Recycle", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))

        StepItem("1","To add")
        StepItem("2","To add")

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "What can or cannot be recycled",
            fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20)
        )

        InfoCard(
            title = "Can be recycled",
            text = "bla bla",
            isPositive = true
        )
        InfoCard(
            title = "Cannot be recycled",
            text = "bla bla bla",
            isPositive = false
        )
    }
}

@Composable
fun GlassGuideContent(){
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Glass retains 100% of it's purity when you recycle them! However, be extremely careful when handling glass as they are fragile and are prone to expose sharp edges if broken.",
            fontSize = 16.sp, lineHeight = 24.sp, color = Color.DarkGray
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text("How to Recycle", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))

        StepItem("1","For containers and bottles, be sure to empty it's contents  and give it a quick rinse")
        StepItem("2","If there are any removable plastic or plastic parts such as the cap of the bottle, recycle them seperately")
        StepItem("3","Carefully place the intact into the recycling bin. Please don't throw them with force!")

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "What can or cannot be recycled",
            fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20)
        )

        InfoCard(
            title = "Can be recycled",
            text = "bla bla",
            isPositive = true
        )
        InfoCard(
            title = "Cannot be recycled",
            text = "bla bla bla",
            isPositive = false
        )
    }
}

@Composable
fun StepItem(stepNumber:String, text: String){
    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp)
    ){
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(Color(0xFF2E7D32), CircleShape),
            contentAlignment = Alignment.Center
        ){
            Text(stepNumber, color = Color.White, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text,
            fontSize = 16.sp,
            lineHeight = 22.sp,
            color = Color.DarkGray,
            modifier = Modifier
                .weight(1f)
                .padding(top=4.dp)
        )
    }
}

@Composable
fun InfoCard(title:String, text: String, isPositive: Boolean){
    val bgColor = if(isPositive) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
    val iconColor = if(isPositive) Color(0xFF2E7D32) else Color(0xFFC62828)
    val icon = if (isPositive) Icons.Default.CheckCircle else Icons.Default.Warning

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        modifier = Modifier.fillMaxWidth()
    ){
        Row(
            modifier = Modifier
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ){
            Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column{
                Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = iconColor)
                Spacer(modifier = Modifier.width(6.dp))
                Text(text, fontSize = 15.sp, color = Color.DarkGray, lineHeight = 22.sp )
            }
        }
    }
}
