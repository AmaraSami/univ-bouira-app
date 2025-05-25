package com.example.univbouira.models

data class GroupItem(
    val groupId: String = "",        // "L3_GROUPE_01" - Full ID with level prefix
    val groupName: String = "",      // "GROUPE 01" - Display name without level
    val level: String = ""           // "L3" - Level extracted from ID
) {
    companion object {
        fun fromDocumentId(documentId: String): GroupItem {
            val parts = documentId.split("_")
            return if (parts.size >= 2) {
                val level = parts[0]  // "L3"
                val groupName = parts.drop(1).joinToString(" ")  // "GROUPE 01"
                GroupItem(
                    groupId = documentId,
                    groupName = groupName,
                    level = level
                )
            } else {
                // Fallback for old format
                GroupItem(
                    groupId = documentId,
                    groupName = documentId,
                    level = ""
                )
            }
        }
    }
}