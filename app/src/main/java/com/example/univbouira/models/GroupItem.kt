package com.example.univbouira.models

/**
 * GroupItem - Helper class to handle group ID formatting
 * Works with database format like "L3_GROUPE_01"
 */
data class GroupItem(
    val documentId: String,     // e.g., "L3_GROUPE_01"
    val level: String,          // e.g., "L3"
    val groupName: String,      // e.g., "Group 1"
    val groupNumber: String     // e.g., "01"
) {

    // Backward compatibility property for existing code
    val groupId: String get() = documentId

    companion object {
        /**
         * Creates a GroupItem from a Firebase document ID
         * @param documentId Format: "L3_GROUPE_01", "L2_GROUPE_02", etc.
         * @return GroupItem with parsed information
         */
        fun fromDocumentId(documentId: String): GroupItem {
            return try {
                when {
                    // Handle database format: "L3_GROUPE_01"
                    documentId.contains("_GROUPE_") -> {
                        val parts = documentId.split("_")
                        if (parts.size >= 3) {
                            val level = parts[0] // "L3"
                            val groupNumber = parts[2] // "01"
                            val displayNumber = groupNumber.toIntOrNull()?.toString() ?: groupNumber
                            val displayName = "Group $displayNumber"

                            GroupItem(
                                documentId = documentId,
                                level = level,
                                groupName = displayName,
                                groupNumber = groupNumber
                            )
                        } else {
                            // Fallback if format is unexpected
                            GroupItem(
                                documentId = documentId,
                                level = "Unknown",
                                groupName = documentId,
                                groupNumber = "01"
                            )
                        }
                    }
                    // Handle simple format: "G1", "G2"
                    documentId.matches(Regex("^G\\d+$")) -> {
                        val groupNumber = documentId.substring(1).padStart(2, '0')
                        val displayName = "Group ${groupNumber.toInt()}"

                        GroupItem(
                            documentId = documentId,
                            level = "Unknown",
                            groupName = displayName,
                            groupNumber = groupNumber
                        )
                    }
                    // Handle legacy formats: "GROUPE 01", "Group 1", etc.
                    documentId.contains("GROUPE") || documentId.contains("Group") -> {
                        val groupNumber = documentId.replace(Regex("[^\\d]"), "").ifEmpty { "1" }.padStart(2, '0')
                        val displayName = "Group ${groupNumber.toInt()}"

                        GroupItem(
                            documentId = documentId,
                            level = "Unknown",
                            groupName = displayName,
                            groupNumber = groupNumber
                        )
                    }
                    // Fallback for any other format
                    else -> {
                        GroupItem(
                            documentId = documentId,
                            level = "Unknown",
                            groupName = documentId,
                            groupNumber = "01"
                        )
                    }
                }
            } catch (e: Exception) {
                // Fallback for any parsing errors
                GroupItem(
                    documentId = documentId,
                    level = "Unknown",
                    groupName = documentId,
                    groupNumber = "01"
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
            return fromDocumentId(groupId)
        }

        /**
         * Creates GroupItems for a specific level in the correct database format
         * @param level The level (e.g., "L1", "L3")
         * @param count Number of groups to create
         * @return List of GroupItems for the level
         */
        fun createGroupsForLevel(level: String, count: Int = 10): List<GroupItem> {
            return (1..count).map { groupNumber ->
                val formattedNumber = groupNumber.toString().padStart(2, '0')
                val documentId = "${level}_GROUPE_$formattedNumber"

                GroupItem(
                    documentId = documentId,
                    level = level,
                    groupName = "Group $groupNumber",
                    groupNumber = formattedNumber
                )
            }
        }

        /**
         * Convert simple group name to database format
         * @param groupName Simple name like "G1", "Group 1", "GROUPE 01"
         * @param level The level like "L3"
         * @return Database format like "L3_GROUPE_01"
         */
        fun toDatabaseFormat(groupName: String, level: String): String {
            val groupNumber = groupName.replace(Regex("[^\\d]"), "").ifEmpty { "1" }
            return "${level}_GROUPE_${groupNumber.padStart(2, '0')}"
        }
    }

    /**
     * Get a short display name for UI (e.g., "G1" from "Group 1")
     */
    fun getShortName(): String {
        return "G${groupNumber.toIntOrNull() ?: groupNumber}"
    }

    /**
     * Get full display text with level (e.g., "L3 - Group 1")
     */
    fun getFullDisplayName(): String {
        return if (level != "Unknown") "$level - $groupName" else groupName
    }

    /**
     * Get legacy format group name (e.g., "GROUPE 01")
     */
    fun getLegacyGroupName(): String {
        return "GROUPE $groupNumber"
    }

    /**
     * Get database document ID for timetable paths
     * Returns full format like "L3_GROUPE_01" to match database structure
     */
    fun getDatabaseId(): String {
        return documentId
    }

    /**
     * Get just the group number as integer
     */
    fun getGroupNumberInt(): Int {
        return groupNumber.toIntOrNull() ?: 1
    }
}