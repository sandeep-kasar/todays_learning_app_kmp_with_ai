package com.todays.learning.utils


// Keep the JSON as a top-level constant
val fallbackTimetableJson = """
{
  "status": "success",
  "result": [
    {
      "time": "09:00 AM - 09:45 AM",
      "subject": {
        "subject": "English",
        "learning": "Reading comprehension and storytelling",
        "homework": "Read page 12-15 of the reader",
        "keyPoints": "Focus on punctuation and sentence structure"
      },
      "standard": 4,
      "division": "A"
    },
    {
      "time": "09:50 AM - 10:35 AM",
      "subject": {
        "subject": "Mathematics",
        "learning": "Introduction to multiplication tables",
        "homework": "Practice tables of 6 and 7",
        "keyPoints": "Understanding repeated addition"
      },
      "standard": 4,
      "division": "A"
    },
    {
      "time": "10:40 AM - 11:25 AM",
      "subject": {
        "subject": "Environmental Studies",
        "learning": "Parts of a plant",
        "homework": "Draw and label parts of a plant",
        "keyPoints": "Root, stem, leaves, flowers"
      },
      "standard": 4,
      "division": "A"
    },
    {
      "time": "11:30 AM - 12:15 PM",
      "subject": {
        "subject": "Hindi",
        "learning": "Reading 'Ekta Mein Bal' story",
        "homework": "Write 5 new words from the story",
        "keyPoints": "Vocabulary building and moral values"
      },
      "standard": 4,
      "division": "A"
    },
    {
      "time": "12:20 PM - 01:05 PM",
      "subject": {
        "subject": "Computer Science",
        "learning": "Basics of computer hardware",
        "homework": "List 3 input and 3 output devices",
        "keyPoints": "Monitor, keyboard, mouse, CPU"
      },
      "standard": 4,
      "division": "A"
    }
  ]
}
""".trimIndent()


val fallbackSubjectJson = """
{
  "status": "success",
  "result": {
    "subject": "English",
    "learning": "In this lesson, students will explore the structure of a story including its introduction, rising action, climax, and resolution. They will read a short fictional narrative and identify key elements such as the main characters, setting, conflict, and moral. Through guided discussion and activities, students will analyze how the author builds tension and develops characters. The aim is to improve reading comprehension, interpretive thinking, and storytelling skills. They will also compare the structure of different stories to gain a deeper understanding of narrative techniques.",
    "homework": "Read pages 12–15 of the English Reader and answer the comprehension questions in your notebook.",
    "keyPoints": "Story structure, plot development, character analysis, moral interpretation"
  }
}
""".trimIndent()

