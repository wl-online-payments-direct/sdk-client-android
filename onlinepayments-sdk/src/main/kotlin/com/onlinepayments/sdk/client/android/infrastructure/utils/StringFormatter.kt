/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2026 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.infrastructure.utils

import com.onlinepayments.sdk.client.android.domain.FormatResult
import java.io.Serializable
import java.security.InvalidParameterException

/**
 * Contains util methods for string formatting.
 */
@Suppress("CyclomaticComplexMethod", "LongMethod", "LoopWithTooManyJumpStatements")
class StringFormatter private constructor() : Serializable {
    companion object {
        private enum class ChangeType {
            ADDED,
            REMOVED,
            REPLACED
        }

        /**
         * Applies a mask to a String, based on the previous value and splice information.
         * The result is a [FormatResult] object, that holds the masked String and the new cursor index.
         * This masker is meant for user input fields, where users are still entering their information.
         *
         * @param mask the mask that will be applied. Characters to be masked should be surrounded by double accolades,
         * mask characters should be between accolade-blocks. E.g. a mask for an expiry date would look like
         * this: {{99}}/{{99}}
         * @param value the value that the mask will be applied to
         * @param oldValue the value that was in the edit text, before characters were removed or added
         * @param start the index of the start of the change
         * @param count the number of characters that were removed
         * @param after the number of characters that were added
         *
         * @return [FormatResult] that contains the masked String and the new cursor index
         */
        @JvmStatic
        fun applyMask(
            mask: String?,
            value: String?,
            oldValue: String?,
            start: Int,
            count: Int,
            after: Int
        ): FormatResult? {
            if (mask == null || value == null || oldValue == null) {
                return null
            }

            // Determine whether character(s) are added, removed, or replaced, and act accordingly.
            val result = when (addedRemovedOrReplaced(count, after)) {
                ChangeType.ADDED -> determineFormatResultADDED(mask, value, oldValue, after, start)
                ChangeType.REMOVED -> determineFormatResultREMOVED(mask, value, start)
                ChangeType.REPLACED -> determineFormatResultREPLACED(mask, value, oldValue, after, start)
            }

            return result
        }

        // Determines what has happened to the text in the editText, using the information that
        // TextWatcher::beforeTextChanged provides.
        private fun addedRemovedOrReplaced(count: Int, after: Int): ChangeType {
            // after > 0 if characters have been added to the string.
            // count > 0 if characters have been removed from the string.
            // both should never be < 0.
            if (count < 0 || after < 0) {
                throw InvalidParameterException("Error when determining mask; count and/or after may not be < 0")
            }

            return if (after != 0 && count == 0) {
                ChangeType.ADDED
            } else if (after == 0 && count != 0) {
                ChangeType.REMOVED
            } else {
                ChangeType.REPLACED
            }
        }

        private fun determineFormatResultADDED(
            mask: String,
            value: String,
            oldValue: String,
            numAddedCharacters: Int,
            previousCursorIndex: Int
        ): FormatResult {
            var formattedValue = ""
            var isInMask = 0
            var valueIndex = 0
            var oldValueIndex = 0

            var newCursorIndex = previousCursorIndex
            var numNewCharactersAdded = 0
            var cursorMoving = false

            // loop over the mask characters
            var maskIndex = 0
            while (maskIndex < mask.length) {
                val maskCharacter = mask[maskIndex]
                if (maskCharacter == '}') {
                    isInMask--
                } else if (maskCharacter == '{') {
                    isInMask++
                } else if (isInMask == 2) {
                    if (valueIndex >= value.length) {
                        break
                    }

                    // If the valueIndex passes the previousCursorLocation, we start moving the cursor
                    // so it will move along with the characters that are being added.
                    if (valueIndex == previousCursorIndex) {
                        cursorMoving = true
                    }

                    // If the numNewCharactersAdded counter equals the number of characters that were
                    // added by the user, the cursor should stop moving
                    if (numNewCharactersAdded == numAddedCharacters) {
                        cursorMoving = false
                    }

                    // If the character is a valid character, we add it to the formattedValue and update
                    // relevant indices.
                    if ((value[valueIndex].toString().matches
                            (convertMaskCharacterToRegex(maskCharacter.toString()).toRegex()))
                    ) {
                        formattedValue += value[valueIndex]

                        if (cursorMoving) {
                            newCursorIndex++
                            // If the cursor is moving, this implicitly means that we are adding new
                            // characters that were not in the oldValue, so we have to update the
                            // numNewCharactersAdded counter
                            numNewCharactersAdded++
                        }
                    } else {
                        // An invalid character had been inserted here; ignore it, but do not move
                        // further through the mask
                        maskIndex--
                    }

                    // If the cursor is not moving, this means that the new numbers are already added,
                    // or that they still need to be added. Either way we need to move along both the
                    // old value and the new value
                    if (!cursorMoving) {
                        oldValueIndex++
                    }
                    valueIndex++
                } else if (isInMask == 0) {
                    // Add the mask character that belongs here

                    formattedValue += maskCharacter

                    // If the valueIndex goes past the length of the value, we are done with formatting
                    // and we can break the loop.
                    if (valueIndex >= value.length) {
                        if (cursorMoving) {
                            newCursorIndex++
                        }
                        break
                    }

                    // If the valueIndex passes the previousCursorLocation, we start moving the cursor
                    // so it will move along with the characters that are being added.
                    if (valueIndex == previousCursorIndex) {
                        cursorMoving = true
                    }

                    // If the cursor is moving, update its location
                    if (cursorMoving) {
                        newCursorIndex++
                    }

                    // If the numNewCharactersAdded counter equals the number of characters that were
                    // added by the user, the cursor should stop moving
                    if (numNewCharactersAdded == numAddedCharacters) {
                        cursorMoving = false
                    }

                    // If no character was added yet and the characters are equal to the mask character
                    // here, this means that the indices can be updated.
                    if (valueIndex == oldValueIndex && value[valueIndex] == maskCharacter
                        && oldValue[oldValueIndex] == maskCharacter
                    ) {
                        valueIndex++
                        oldValueIndex++
                    }
                } else {
                    throw InvalidParameterException("Error while masking inputText; there seems to be an invalid mask.")
                }
                maskIndex++
            }
            return FormatResult(formattedValue, newCursorIndex)
        }

        private fun determineFormatResultREMOVED(
            mask: String,
            value: String,
            newCursorLocation: Int
        ): FormatResult {
            // Variables used to determine the new Formatted value

            var formattedValue = ""
            var isInMask = 0
            var index = 0

            // Variables used to determine the new Cursor index
            val newCursorIndex = newCursorLocation

            // loop over the mask characters
            var i = 0
            while (i < mask.length) {
                val maskCharacter = mask[i]
                if (maskCharacter == '}') {
                    isInMask--
                } else if (maskCharacter == '{') {
                    isInMask++
                } else if (isInMask == 2) {
                    // If the index reached the end of the value; we are done

                    if (index >= value.length) {
                        break
                    }

                    // If the character is valid, add it to the new value and update the index
                    if ((value[index].toString().matches
                            (convertMaskCharacterToRegex(maskCharacter.toString()).toRegex()))
                    ) {
                        formattedValue += value[index]
                    } else {
                        // An invalid character has been detected; we have to go one step back in the
                        // mask in order to mask the valid characters correctly.
                        i--
                    }

                    index++
                } else if (isInMask == 0) {
                    if (index >= value.length) {
                        // If the end of value is reached we can break the loop, but not before adding
                        // the mask-character that belongs at the end.
                        formattedValue += maskCharacter
                        break
                    } else if (value[index].toString() == maskCharacter.toString()) {
                        // If the masked character was already in the old string add it to the formatted
                        // result and update the index.
                        formattedValue += value[index]
                        index++
                    } else {
                        // If the masked character was not already in the old value, add it. Do not
                        // update the index, but do move the cursor by 1.
                        formattedValue += maskCharacter
                    }
                } else {
                    error("Error while masking inputText; there seems to be an invalid mask.")
                }

                i++
            }

            return FormatResult(formattedValue, newCursorIndex)
        }

        private fun determineFormatResultREPLACED(
            mask: String,
            value: String,
            oldValue: String,
            numAddedCharacters: Int,
            previousCursorIndex: Int
        ): FormatResult {
            val intermediateResult = determineFormatResultREMOVED(mask, value, previousCursorIndex)

            val finalResult = determineFormatResultADDED(
                mask,
                intermediateResult.formattedResult ?: "",
                oldValue,
                numAddedCharacters,
                previousCursorIndex
            )

            return finalResult
        }

        /**
         * Will apply a mask and returns a [FormatResult] that holds the masked String and the new cursor index.
         *
         * @param mask the mask that will be applied
         * @param value the value that the mask will be applied to
         * @param cursorIndex the cursorIndex before applying the changes
         *
         * @return [FormatResult], containing the formatted value and the new cursor index
         */
        @JvmStatic
        fun applyMask(mask: String?, value: String?, cursorIndex: Int): FormatResult? {
            if (mask == null || value == null) {
                return null
            }

            return getFormatResult(mask, value, cursorIndex)
        }

        /**
         * Method calculates the masked value and the cursorIndex.
         *
         * @param mask the mask that will be applied
         * @param value the String that the mask will be applied to
         * @param cursorIndex the cursorIndex before applying the changes
         *
         * @return [FormatResult], containing the formatted value and the cursor index
         */
        private fun getFormatResult(mask: String?, value: String?, cursorIndex: Int): FormatResult? {
            var cursorIndex = cursorIndex
            if (mask == null || value == null) {
                return null
            }

            var newValue = ""

            var isInMask = 0
            var index = 0

            // loop over the mask characters
            var i = 0
            while (i < mask.length) {
                val maskCharacter = mask[i]
                if (maskCharacter == '}') {
                    isInMask--
                } else if (maskCharacter == '{') {
                    isInMask++
                } else {
                    // if character between {{ and }}, add character at the same position of the value

                    if (isInMask == 2) {
                        // if the index is higher than the value which has to be masked
                        // then break function

                        if (index >= value.length) {
                            break
                        }

                        // If this is a valid character, add it to the formattedValue
                        if ((value[index].toString().matches
                                (convertMaskCharacterToRegex(maskCharacter.toString()).toRegex()))
                        ) {
                            newValue += value[index]
                        } else {
                            // if characters are not equal, then go 1 step back in the mask (there has been added or removed something)

                            i--
                        }
                        index++

                        // if character between {{}} blocks, then add mask values
                    } else if (isInMask == 0) {
                        if (index < value.length) {
                            // check whether cursor has to be moved 1 place
                            if (index == cursorIndex) {
                                cursorIndex++
                            }

                            //check whether index has to be moved 1 place
                            if (value[index] == maskCharacter) {
                                index++
                            }
                        }

                        newValue += maskCharacter

                        if (index >= value.length) {
                            break
                        }
                    }
                }
                i++
            }

            return FormatResult(newValue, cursorIndex)
        }

        /**
         * Applies the mask to the value. This method can be used if you want to mask a static String.
         *
         * @param mask the mask that should be applied
         * @param value the String that the mask is applied to
         *
         * @return the masked value
         */
        @JvmStatic
        fun applyMask(mask: String?, value: String?): String? {
            return getFormatResult(mask, value, 0)!!.formattedResult?.trim()
        }

        /**
         * Removes the mask on a given value.
         *
         * @param mask the mask that is applied to the value
         * @param value the value of which the mask will be removed
         *
         * @return the unmasked value
         */
        @JvmStatic
        fun removeMask(mask: String?, value: String?): String? {
            var value = value
            if (mask == null || value == null) {
                return null
            }

            // First apply Mask on the value
            // So this method will work if you put in any value (masked or unmasked)
            value = applyMask(mask, value)

            var newValue = ""

            var isInMask = 0
            var index = 0

            // loop over the mask characters
            for (i in 0 until mask.length) {
                val maskCharacter = mask[i]
                if (maskCharacter == '}') {
                    isInMask--
                } else if (maskCharacter == '{') {
                    isInMask++
                } else {
                    // if between {{ and }}, add position of value to newValue
                    if (isInMask == 2) {
                        if (index >= value!!.length) {
                            break
                        }

                        newValue += value[index]
                    }

                    index++
                }
            }

            return newValue
        }

        /**
         * Relaxes the given mask, meaning it will also mask characters not corresponding to the initial mask.
         *
         * @param mask the mask that should be relaxed
         *
         * @return the relaxed mask
         */
        @Suppress("unused")
        @JvmStatic
        fun relaxMask(mask: String): String {
            var replaceCharacters = false
            val relaxedMask = StringBuilder()

            // loop over the mask characters
            for (i in 0 until mask.length) {
                val maskCharacter = mask[i]
                if (maskCharacter == '}') {
                    replaceCharacters = false
                    relaxedMask.append("}")
                } else if (maskCharacter == '{') {
                    replaceCharacters = true
                    relaxedMask.append("{")
                } else {
                    // if between {{ and }}, replace the character with a *
                    if (replaceCharacters) {
                        relaxedMask.append("*")
                    } else {
                        relaxedMask.append(" ")
                    }
                }
            }

            return relaxedMask.toString()
        }

        /**
         * Replaces a mask character with a regex so it can be used for matching.
         *
         * @param maskCharacter the character whose regex version should be returned
         *
         * @return the regex version of the maskCharacter
         */
        private fun convertMaskCharacterToRegex(maskCharacter: String): String {
            return maskCharacter.replace("9".toRegex(), "[\\\\dX]")
                .replace("a".toRegex(), "[a-z]")
                .replace("A".toRegex(), "[A-Z]")
                .replace("\\*".toRegex(), ".*")
        }

        @Suppress("Unused")
        private const val serialVersionUID = -365568554479175934L
    }
}

