package com.example.smartwaste.data

data class QuizQuestions (
    val question: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val explanation: String
)

//Quiz Bank

val fullQustionBank = listOf(
    QuizQuestions(
        question = "Can you recycle a pizza box that has food stains?" ,
        options = listOf("Of course, its a paper product", "Nope, dispose it as general waste", "Only after rinsing it"),
        correctAnswerIndex = 1 ,
        explanation = "Paper packaging that is contaminated with food (such as greasy pizza boxes) cannot be placed in the blue recycling bins!"
    ),
    QuizQuestions(
        question = "What should you do with the used styrofoam food containers/boxes?",
        options = listOf("Dispose them as general waste","Rinse and recycle them","Recycle them"),
        correctAnswerIndex = 0 ,
        explanation = "Styrofoam food containers/boxes cannot be recycled. Dispose them as general waste!"
    ),
    QuizQuestions(
        question = "Are paper receipts allowed to be recycled?",
        options = listOf("Yes, they are paper products", "Nope, the ink causes the paper to be contaminated", "Only thermal receipts"),
        correctAnswerIndex = 0,
        explanation = "All paper receipts are allowed to be recycled in the blue bins!"
    ),
    QuizQuestions(
        question = "What cannot be recycled?",
        options = listOf("Egg trays", "Paint cans", "Glitter papers"),
        correctAnswerIndex = 2,
        explanation = "Since there is a non-paper layer, it is not practical for glitter papers to be recycled!"
    ),
    QuizQuestions(
        question = "Can used tissues and paper towels go in the blue bin?" ,
        options = listOf("Yes, but only if they are dry","No, dispose them as general waste","They are paper products, recycle them"),
        correctAnswerIndex = 1,
        explanation = "used tissues and paper towels are often unclean and not selected for recycling!"
    ),
    QuizQuestions(
        question = "When recycling plastic product, what is should you do before recycling them",
        options = listOf("Check for the expiry date", "Break them into smaller pieces for easier recycling", "Rinse them if they are dirty"),
        correctAnswerIndex = 2 ,
        explanation = "If the plastic product is dirty, give them a rinse first before recycling!"
    ),
    QuizQuestions(
        question = "Which of the item can be recycled?",
        options = listOf("Toys","Egg trays","Crayon drawings"),
        correctAnswerIndex = 1,
        explanation = "Egg trays are perfectly fine to be recycled as long as they are not contaminated. Donate toys if it can be reused!"
    ),
    QuizQuestions(
        question = "Should you recycle a recyclable item if it is contaminated?",
        options = listOf("No, dispose them as general waste", "Yes, since they are still recyclable product", "Yes, after giving them a rinse"),
        correctAnswerIndex = 0,
        explanation = "Do not recycle a product if they are contaminated, even if it is a recyclable product!"
    ),
    QuizQuestions(
        question = "Can metals be recycled?",
        options = listOf("No, they pose a safety risk", "Yes, all metals can be recycled", "Only if they are not electronics, rusty or contaminated"),
        correctAnswerIndex = 2,
        explanation = "Some metals are generally okay to be recycled!"
    ),
    QuizQuestions(
        question = "My disposable plastic cutlery is contaminated what should I recycle them?",
        options = listOf("Yes, after cutting the part that is contaminated out and recycle the rest", "No, dispose them as general waste", "Just recycle it"),
        correctAnswerIndex = 1,
        explanation = "Do not recycle a product if they are contaminated, even if it is a recyclable product!"
    ),
    QuizQuestions(
        question = "What item cannot be recycled?",
        options = listOf("Glossy paper", "Envelope with plastic window", "Wax paper"),
        correctAnswerIndex = 2,
        explanation = "Dispose Wax paper as general waste!"
    ),
    QuizQuestions(
        question = "Which of the following item can be recycled?",
        options = listOf("Cosmetic glass bottle", "Tempered glass", "Crystal glass"),
        correctAnswerIndex = 0,
        explanation = "Please do not recycle tempered glass and crystal glass."
    ),
    QuizQuestions(
        question = "Which of the following item can be recycled?",
        options = listOf("Steel wool", "Tempered glass", "Wax paper"),
        correctAnswerIndex = 0,
        explanation = "Steel wool can be recycled!"
    ),
    QuizQuestions(
        question = "What item cannot be recycled?",
        options = listOf("Receipts", "Ceramics", "Steel wool"),
        correctAnswerIndex = 2,
        explanation = "Disposed Ceramics carefully and do not recycle them!"
    )
)
