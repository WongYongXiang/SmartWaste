package com.example.smartwaste.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartwaste.data.fullQustionBank

@Composable
fun QuizScreen(onQuizComplete: (Int) -> Unit, onNavigateBack: () -> Unit) {
    val questions by remember { mutableStateOf(fullQustionBank.shuffled().take(5)) }
    var currentQuestionsIndex by remember { mutableStateOf(0) }
    var score by remember { mutableStateOf(0) }
    var selectedAnswerIndex by remember { mutableStateOf<Int?>(null) }
    var showExplanation by remember { mutableStateOf(false) }
    var isQuizFinished by remember {mutableStateOf(false)}
    var showExitDialog by remember {mutableStateOf(false)} // in case the user want to use back button to quit

    BackHandler(enabled = !isQuizFinished) {
        showExitDialog = true
    }

    if (showExitDialog){
        AlertDialog(
            onDismissRequest = { showExitDialog = false},
            containerColor = Color.White,
            iconContentColor = Color(0xFF2E7D32),
            titleContentColor = Color(0xFF1B5E20),
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Warning",
                    modifier = Modifier.size(32.dp)
                )
            },
            title = { Text("Exit quiz?", fontWeight = FontWeight.Bold)},
            text ={
                Text(
                    text = "Are you sure? Your progress will not be saved and points will not be earned",
                    color = Color.DarkGray,
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExitDialog = false
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD32F2F),
                        contentColor = Color.White
                    )
                ) {
                    Text("Quit", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {showExitDialog = false},
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF2E7D32)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E7D32))
                ) {
                    Text("Continue", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFCFA))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isQuizFinished){
            Spacer(modifier = Modifier.height(60.dp))
            Icon(Icons.Default.CheckCircleOutline, contentDescription = null, tint = Color(0xFFFBC02D), modifier = Modifier.size(80.dp))
            Spacer(modifier = Modifier.height(24.dp))
            Text("Quiz Complete!", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1B5E20))
            Spacer(modifier = Modifier.height(16.dp))
            Text("You have scored $score / 5 questions correct", fontSize = 24.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("You have earned ${score*20} points!", fontSize = 20.sp, color = Color.DarkGray)
            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = {onQuizComplete(score*20)},
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ){
                Text("Return", fontSize=18.sp, fontWeight = FontWeight.Bold)
            }

        } else {
            val currentQuestion = questions[currentQuestionsIndex]

            Text(
                text = "Question ${currentQuestionsIndex +1} of 5",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFAFCFA),
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = currentQuestion.question,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1B5E20),
                textAlign = TextAlign.Center,
                lineHeight = 32.sp
            )
            Spacer(modifier = Modifier.height(40.dp))

            currentQuestion.options.forEachIndexed { index, optionText ->
                val isSelected = selectedAnswerIndex == index
                val isCorrect = index == currentQuestion.correctAnswerIndex

                val buttonColor = if (showExplanation) {
                    when {
                        isCorrect -> Color(0xFF4CAF50) //Correct ans will be highlighted (GREEN)
                        isSelected && !isCorrect -> Color(0xFFD32F2F) //wrong choice highlighted (RED)
                        else -> Color(0xFFE8F5E9)
                    }
                } else {
                    Color(0xFFE8F5E9)
                }
                val textColor = if (showExplanation && (isCorrect || (isSelected && !isCorrect))) {
                    Color.White
                } else {
                    Color(0xFF1B5E20)
                }
                Button(
                    onClick = {
                        if (!showExplanation) {
                            selectedAnswerIndex = index
                            showExplanation = true
                            if (isCorrect) score++
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .height(65.dp)
                ) {
                    Text(
                        text = optionText,
                        color = textColor,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
                if (showExplanation) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = currentQuestion.explanation,
                            modifier = Modifier.padding(16.dp),
                            color = Color.DarkGray,
                            fontSize = 15.sp,
                            lineHeight = 22.sp
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Button(
                        onClick = {
                            if (currentQuestionsIndex <4){
                                currentQuestionsIndex++
                                selectedAnswerIndex = null
                                showExplanation = false
                            } else {
                                isQuizFinished = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2E7D32),
                            contentColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ){
                        Text(if(currentQuestionsIndex< 4) "Next Question" else "See Results", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
