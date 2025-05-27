package com.example.univbouira.models

/**
 * GroupItem - Helper class to handle group ID formatting
 * Converts between document IDs (L1_GROUPE_01) and display names (Group 1)
 */
data class GroupItem(
    val documentId: String,     // e.g., "L1_GROUPE_01"
    val level: String,          // e.g., "L1"
    val groupName: String,      // e.g., "Group 1"
    val groupNumber: String     // e.g., "01"
) {

    // Backward compatibility property for existing code
    val groupId: String get() = documentId

    companion object {
        /**
         * Creates a GroupItem from a Firebase document ID
         * @param documentId Format: "L1_GROUPE_01", "L2_GROUPE_02", etc.
         * @return GroupItem with parsed information
         */
        fun fromDocumentId(documentId: String): GroupItem {
            return try {
                // Handle different possible formats
                when {
                    // New format: "L1_GROUPE_01"
                    documentId.contains("_GROUPE_") -> {
                        val parts = documentId.split("_")
                        val level = parts[0] // "L1"
                        val groupNumber = parts[2] // "01"
                        val displayName = "Group ${groupNumber.toIntOrNull() ?: groupNumber}"

                        GroupItem(
                            documentId = documentId,
                            level = level,
                            groupName = displayName,
                            groupNumber = groupNumber
                        )
                    }
                    // Legacy format: "GROUPE 01", "Group 1", etc.
                    documentId.contains("GROUPE") -> {
                        val groupNumber = documentId.replace("GROUPE", "").trim()
                        val displayName = "Group ${groupNumber.toIntOrNull() ?: groupNumber}"

                        GroupItem(
                            documentId = documentId,
                            level = "Unknown",
                            groupName = displayName,
                            groupNumber = groupNumber
                        )
                    }
                    // Handle "Group X" format
                    documentId.startsWith("Group ") -> {
                        val groupNumber = documentId.replace("Group ", "").trim()

                        GroupItem(
                            documentId = documentId,
                            level = "Unknown",
                            groupName = documentId, // Keep as is
                            groupNumber = groupNumber
                        )
                    }
                    // Fallback for any other format
                    else -> {
                        GroupItem(
                            documentId = documentId,
                            level = "Unknown",
                            groupName = documentId,
                            groupNumber = "Unknown"
                        )
                    }
                }
            } catch (e: Exception) {
                // Fallback for any parsing errors
                GroupItem(
                    documentId = documentId,
                    level = "Unknown",
                    groupName = documentId,
                    groupNumber = "Unknown"
                )
            }
        }

        /**
         * Creates a list of GroupItems from a list of document IDs
         */
        fun fromDocumentIds(documentIds: List<String>): List<GroupItem> {
            return documentIds.map { fromDocumentId(it) }
        }

        /**
         * Creates a GroupItem for backward compatibility with old structure
         * @param groupId The group identifier (can be document ID or display name)
         * @param level Optional level information
         */
        fun fromLegacyGroupId(groupId: String, level: String = "Unknown"): GroupItem {
            return fromDocumentId(groupId).copy(level = level)
        }
    }

    /**
     * Get a short display name for UI (e.g., "G1" from "Group 1")
     */
    fun getShortName(): String {
        return groupName.replace("Group ", "G")
    }

    /**
     * Get full display text with level (e.g., "L1 - Group 1")
     */
    fun getFullDisplayName(): String {
        return if (level != "Unknown") "$level - $groupName" else groupName
    }

    /**
     * Get legacy format group name (e.g., "GROUPE 01")
     */
    fun getLegacyGroupName(): String {
        return "GROUPE ${groupNumber.padStart(2, '0')}"
    }
}