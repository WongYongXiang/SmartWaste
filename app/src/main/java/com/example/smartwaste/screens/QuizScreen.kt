package com.example.smartwaste.screens

import androidx.compose.foundation.layout.*
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isQuizFinished){
            Spacer(modifier = Modifier.height(40.dp))
            Text("Quiz Complete!", fontSize = 30.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Text("You have scored $score / 5 questions correct", fontSize = 24.sp, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(10.dp))
            Text("You have earned ${score*20} points!", fontSize = 20.sp)
            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = {onQuizComplete(score*20)},
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ){
                Text("Return", fontSize=18.sp)
            }

        } else {
            val currentQuestion = questions[currentQuestionsIndex]

            Text(
                text = "Question ${currentQuestionsIndex +1} of 5",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = currentQuestion.question,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(30.dp))

            currentQuestion.options.forEachIndexed { index, optionText ->
                val isSelected = selectedAnswerIndex == index
                val isCorrect = index == currentQuestion.correctAnswerIndex

                val buttonColor = if (showExplanation) {
                    when {
                        isCorrect -> Color(0xFF4CAF50) //Correct ans will be highlighted (GREEN)
                        isSelected && !isCorrect -> Color(0xFFD32F2F) //wrong choice highlighted (RED)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
                val textColor = if (showExplanation && (isCorrect || (isSelected && !isCorrect))) {
                    Color.White
                } else {
                    MaterialTheme.colorScheme.onSurface
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .height(60.dp)
                ) {
                    Text(
                        text = optionText,
                        color = textColor,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
                if (showExplanation) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                        Text(
                            text = currentQuestion.explanation,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onSecondaryContainer
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
                        }
                    ){
                        Text(if(currentQuestionsIndex< 4) "Next Question" else "See Results")
                    }
                }
            }
        }
    }
