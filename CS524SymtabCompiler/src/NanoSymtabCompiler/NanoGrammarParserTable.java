// File generated by Invisible Jacc version 1.1.
// Invisible Jacc is Copyright 1997-1998 Invisible Software, Inc.

package NanoSymtabCompiler;

import invisible.jacc.parse.ParserTable;
import invisible.jacc.util.ArrayRLE;

public class NanoGrammarParserTable extends ParserTable
{

    // The number of symbols.

    private static final int gen_symbolCount = 95;

    // The number of productions.

    private static final int gen_productionCount = 94;

    // The symbol on the left hand side of each production.

    private static final int[] gen_productionLHSSymbol = 
    {46, 45, 48, 48, 49, 49, 50, 50, 54, 54, 47, 55, 51, 52, 56, 59, 59, 57, 57, 62,
    62, 58, 64, 65, 66, 66, 66, 68, 61, 61, 63, 63, 53, 53, 53, 53, 53, 53, 53, 53,
    67, 76, 78, 77, 69, 79, 79, 70, 81, 81, 82, 82, 71, 71, 72, 72, 83, 84, 85, 86,
    73, 87, 74, 75, 75, 88, 88, 80, 80, 80, 80, 89, 89, 89, 89, 60, 60, 60, 90, 90,
    90, 90, 90, 92, 92, 93, 93, 93, 93, 93, 93, 91, 91, 94};

    // The length of the right hand side of each production.

    private static final int[] gen_productionRHSLength = 
    {1, 13, 2, 0, 2, 0, 2, 0, 2, 0, 0, 0, 0, 0, 5, 3, 1, 5, 5, 6,
    4, 2, 6, 1, 3, 1, 0, 3, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
    11, 0, 0, 0, 6, 0, 3, 6, 0, 3, 1, 4, 4, 7, 2, 2, 2, 2, 5, 0,
    7, 2, 2, 5, 6, 3, 1, 3, 3, 3, 1, 3, 3, 3, 1, 1, 2, 2, 1, 1,
    1, 3, 5, 1, 4, 1, 1, 1, 1, 1, 1, 1, 1, 2};

    // The parameter for each production.

    private static final int[] gen_productionParam = 
    {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    // The maximum number of insertions during error repair.

    private static final int gen_maxInsertion = 100;

    // The maximum number of deletions during error repair.

    private static final int gen_maxDeletion = 200;

    // The validation length for error repair.

    private static final int gen_validationLength = 5;

    // The number of single-point insertions for error repair.

    private static final int gen_singlePointInsertionCount = 44;

    // The list of symbols for single-point insertions.

    private static final int[] gen_singlePointInsertions = 
    {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20,
    21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40,
    41, 42, 43, 44};

    // The goal production.

    private static final int gen_goalProduction = 93;

    // The end-of-file symbol.

    private static final int gen_eofSymbol = 0;

    // Insertion cost of each symbol for error repair.

    private static final int[] gen_insertionCost = 
    {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
    1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
    1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    // Deletion cost of each symbol for error repair.

    private static final int[] gen_deletionCost = 
    {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
    1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
    1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    // The number of LR(1) states.

    private static final int gen_stateCount = 130;

    // Parsing action table.

    private static short[][] gen_actionTable = null;
    private static short[] rle_actionTable = 
    {130, 96, 188, -32510, 10, -32510, 188, 10, -32476, 188, 94, 189, 190, -32466, 188, 10, 96, 93, -32419, 188,
    191, 96, 188, -32510, 3, -32510, 188, 3, -32473, 188, 192, -32467, 188, 3, 96, -32418, 188, 93, 96, 188,
    193, -32511, 5, -32510, 188, 5, -32472, 188, 194, -32507, 188, 96, -32475, 188, 5, 96, -32471, 188, 110, -32497,
    188, 195, -32478, 188, 110, 96, -32511, 188, 196, 7, -32510, 188, 7, -32471, 188, 197, -32507, 188, 98, -32476,
    188, 7, 96, -32490, 188, 198, -32502, 188, 199, -32454, 188, 199, 96, -32471, 188, 200, -32497, 188, 201, -32511,
    188, 202, -32481, 188, 200, 96, -32510, 188, 12, -32510, 188, 203, -32470, 188, 204, -32507, 188, 100, -32508, 188,
    205, -32483, 188, 12, 96, -32471, 188, 109, -32461, 188, 109, 96, -32495, 188, 206, -32511, 188, 185, 186, -32510,
    188, 207, -32509, 188, 208, -32503, 188, 209, 172, -32497, 188, 210, -32484, 188, 169, 173, 174, -32511, 188, 172,
    96, -32490, 188, 16, 188, 16, -32511, 188, 211, -32447, 188, 16, 96, -32490, 188, 198, 188, 212, -32444, 188,
    212, 96, -32488, 188, 213, -32444, 188, 213, 96, -32471, 188, 214, -32461, 188, 214, 96, -32510, 188, 215, -32422,
    188, 215, 96, -32510, 188, 216, -32452, 188, 115, 188, 117, -32486, 188, 216, 96, -32492, 188, 185, 186, -32510,
    188, 207, -32498, 188, 209, 172, -32467, 188, 171, 173, 174, -32511, 188, 172, 96, -32495, 188, 206, -32511, 188,
    185, 186, -32510, 188, 207, -32509, 188, 208, -32503, 188, 209, 172, -32497, 188, 168, -32494, 188, 217, -32505, 188,
    218, 169, 173, 174, -32511, 188, 172, 96, -32492, 188, 185, 186, -32510, 188, 207, -32498, 188, 209, 172, -32467,
    188, 170, 173, 174, -32511, 188, 172, 96, -32502, 188, 83, -32511, 188, -32511, 83, -32510, 188, -32511, 83, -32511,
    188, -32511, 83, -32511, 188, 83, 219, -32508, 83, -32511, 188, -32507, 83, -32460, 188, 83, 96, -32489, 188, 108,
    -32443, 188, 108, 96, -32470, 188, 220, -32462, 188, 220, 96, -32508, 188, 122, 123, -32459, 188, 221, -32480, 188,
    122, 96, -32508, 188, 124, 125, -32457, 188, 222, -32482, 188, 124, 96, -32487, 188, 223, -32445, 188, 223, 96,
    -32510, 188, 13, -32509, 188, -32510, 13, -32511, 188, 13, -32511, 188, -32511, 13, -32489, 188, 13, -32504, 188, 224,
    -32471, 188, 13, 96, 188, -32509, 41, -32510, 188, -32510, 41, -32511, 188, 41, -32511, 188, -32511, 41, -32489, 188,
    41, -32480, 188, 225, -32495, 188, 41, 96, -32493, 188, 226, -32507, 188, 175, -32511, 188, 227, 228, -32509, 188,
    179, 184, 180, 182, 181, 183, -32462, 188, 229, 188, 175, 96, -32502, 188, 70, -32511, 188, -32511, 70, -32510,
    188, 230, 70, -32511, 188, -32511, 70, -32511, 188, 70, 188, -32510, 70, 231, 232, -32511, 188, -32507, 70, -32460,
    188, 70, 96, -32495, 188, 206, -32511, 188, 185, 186, -32510, 188, 207, -32509, 188, 208, -32503, 188, 209, 172,
    -32497, 188, 168, -32494, 188, 233, -32505, 188, 218, 169, 173, 174, -32511, 188, 172, 96, -32484, 188, 234, -32448,
    188, 234, 96, -32489, 188, 111, -32443, 188, 111, 96, -32489, 188, 112, -32443, 188, 112, 96, -32486, 188, 26,
    -32499, 188, 110, -32497, 188, 235, -32507, 188, 236, 188, 237, -32487, 188, 26, 96, -32510, 188, 216, -32509, 188,
    238, 239, 240, -32511, 188, 241, -32511, 188, 242, 243, -32489, 188, 244, -32503, 188, 245, -32500, 188, 126, 188,
    127, 128, 129, 130, 131, 132, 133, -32506, 188, 246, -32510, 188, 248, -32506, 188, 242, 96, 188, -32509, 3,
    -32510, 188, -32510, 3, -32511, 188, 3, -32511, 188, -32511, 3, -32489, 188, 3, -32508, 188, 250, -32467, 188, 3,
    96, -32495, 188, 206, -32511, 188, 185, 186, -32510, 188, 207, -32509, 188, 208, -32503, 188, 209, 172, -32497, 188,
    168, -32485, 188, 251, 169, 173, 174, -32511, 188, 172, 96, -32495, 188, 206, -32511, 188, 185, 186, -32510, 188,
    207, -32509, 188, 208, -32503, 188, 209, 172, -32497, 188, 168, -32485, 188, 252, 169, 173, 174, -32511, 188, 172,
    96, -32495, 188, 206, -32511, 188, 185, 186, -32510, 188, 207, -32509, 188, 208, -32503, 188, 209, 172, -32497, 188,
    168, -32485, 188, 253, 169, 173, 174, -32511, 188, 172, 96, -32495, 188, 206, -32511, 188, 185, 186, -32510, 188,
    207, -32509, 188, 208, -32503, 188, 209, 172, -32497, 188, 168, -32494, 188, 254, -32505, 188, 218, 169, 173, 174,
    -32511, 188, 172, 96, -32495, 188, 206, -32511, 188, 185, 186, -32510, 188, 207, -32509, 188, 208, -32503, 188, 209,
    172, -32497, 188, 167, -32484, 188, 169, 173, 174, -32511, 188, 172, 96, -32495, 188, 206, -32511, 188, 185, 186,
    -32510, 188, 207, -32509, 188, 208, -32503, 188, 209, 172, -32497, 188, 165, -32484, 188, 169, 173, 174, -32511, 188,
    172, 96, -32495, 188, 206, -32511, 188, 185, 186, -32510, 188, 207, -32509, 188, 208, -32503, 188, 209, 172, -32497,
    188, 166, -32484, 188, 169, 173, 174, -32511, 188, 172, 96, -32493, 188, 226, -32505, 188, 178, 227, 228, -32450,
    188, 178, 96, -32490, 188, 255, 188, 20, -32444, 188, 20, 96, -32490, 188, 198, 188, 256, -32444, 188, 256,
    96, -32486, 188, 257, -32446, 188, 257, 96, -32489, 188, 258, -32511, 188, 25, -32446, 188, 25, 96, -32487, 188,
    259, -32445, 188, 259, 96, -32487, 188, 260, -32445, 188, 260, 96, -32495, 188, 206, -32511, 188, 185, 186, -32510,
    188, 207, -32509, 188, 208, -32503, 188, 209, 172, -32497, 188, 168, -32494, 188, 261, -32505, 188, 218, 169, 173,
    174, -32511, 188, 172, 96, -32471, 188, 155, -32461, 188, 155, 96, -32489, 188, 156, -32443, 188, 156, 96, -32471,
    188, 262, -32461, 188, 262, 96, -32485, 188, 263, -32508, 188, 264, -32453, 188, 264, 96, -32510, 188, -32511, 9,
    -32510, 188, -32510, 9, -32511, 188, 9, -32511, 188, -32511, 9, -32489, 188, 9, -32502, 188, 265, -32473, 188, 9,
    96, -32502, 188, 266, -32441, 188, 148, 149, -32504, 188, 266, 96, -32502, 188, 267, -32441, 188, 148, 149, -32504,
    188, 267, 96, -32479, 188, 268, -32453, 188, 268, 96, -32479, 188, 269, -32453, 188, 269, 96, 188, 193, -32510,
    5, -32510, 188, -32510, 5, -32511, 188, 5, -32511, 188, -32511, 5, -32489, 188, 5, -32507, 188, 270, -32507, 188,
    96, -32475, 188, 5, 96, -32502, 188, 69, -32511, 188, -32511, 69, -32510, 188, 230, 69, -32511, 188, -32511, 69,
    -32511, 188, 69, 188, -32510, 69, 231, 232, -32511, 188, -32507, 69, -32460, 188, 69, 96, -32502, 188, 67, -32511,
    188, -32511, 67, -32510, 188, 230, 67, -32511, 188, -32511, 67, -32511, 188, 67, 188, -32510, 67, 231, 232, -32511,
    188, -32507, 67, -32460, 188, 67, 96, -32502, 188, 68, -32511, 188, -32511, 68, -32510, 188, 230, 68, -32511, 188,
    -32511, 68, -32511, 188, 68, 188, -32510, 68, 231, 232, -32511, 188, -32507, 68, -32460, 188, 68, 96, -32493, 188,
    226, -32507, 188, 176, -32511, 188, 227, 228, -32450, 188, 176, 96, -32471, 188, 271, -32494, 188, 113, -32481, 188,
    271, 96, -32508, 188, 122, 123, -32459, 188, 121, -32480, 188, 122, 96, -32489, 188, 116, -32443, 188, 116, 96,
    -32486, 188, 26, -32499, 188, 110, -32497, 188, 235, -32507, 188, 118, 188, 237, -32487, 188, 26, 96, -32469, 188,
    272, -32463, 188, 272, 96, -32469, 188, 273, -32463, 188, 273, 96, -32502, 188, 56, -32505, 188, 226, -32504, 188,
    227, 228, -32450, 188, 56, 96, -32487, 188, 274, -32445, 188, 274, 96, -32495, 188, 206, -32511, 188, 185, 186,
    -32510, 188, 207, -32509, 188, 208, -32503, 188, 209, 172, -32497, 188, 168, -32494, 188, 275, -32505, 188, 218, 169,
    173, 174, -32511, 188, 172, 96, -32495, 188, 206, -32511, 188, 185, 186, -32510, 188, 207, -32509, 188, 208, -32503,
    188, 209, 172, -32497, 188, 168, -32494, 188, 276, -32505, 188, 218, 169, 173, 174, -32511, 188, 172, 96, -32510,
    188, 216, 277, -32510, 188, 238, 239, 240, -32511, 188, 241, -32511, 188, 242, 243, -32489, 188, 244, -32503, 188,
    102, -32500, 188, 126, 188, 127, 128, 129, 130, 131, 132, 133, -32506, 188, 246, -32510, 188, 248, -32506, 188,
    277, 96, -32510, 188, 216, -32509, 188, 238, 239, 240, -32511, 188, 241, -32511, 188, 242, 243, -32489, 188, 244,
    -32503, 188, 278, -32500, 188, 126, 188, 127, 128, 129, 130, 131, 132, 133, -32506, 188, 247, -32510, 188, 249,
    -32506, 188, 242, 96, -32510, 188, 216, -32509, 188, 238, 239, 240, -32511, 188, 241, -32511, 188, 242, 243, -32489,
    188, 244, -32503, 188, 279, -32500, 188, 126, 188, 127, 128, 129, 130, 131, 132, 133, -32506, 188, 247, -32510,
    188, 249, -32506, 188, 242, 96, -32495, 188, 206, -32511, 188, 185, 186, -32510, 188, 207, -32509, 188, 208, -32503,
    188, 209, 172, -32497, 188, 168, -32494, 188, 280, -32505, 188, 218, 169, 173, 174, -32511, 188, 172, 96, -32495,
    188, 206, -32511, 188, 185, 186, -32510, 188, 207, -32509, 188, 208, -32503, 188, 209, 172, -32497, 188, 168, -32494,
    188, 281, -32505, 188, 218, 169, 173, 174, -32511, 188, 172, 96, -32511, 188, 196, -32511, 43, -32510, 188, -32510,
    43, -32511, 188, 43, -32511, 188, -32511, 43, -32489, 188, 43, -32499, 188, 98, -32494, 188, 282, -32496, 188, 43,
    96, -32485, 188, 211, -32447, 188, 211, 96, -32490, 188, 45, -32510, 188, 45, -32462, 188, 283, -32498, 188, 45,
    96, -32490, 188, 48, -32510, 188, 48, -32460, 188, 284, -32500, 188, 48, 96, -32495, 188, 206, -32511, 188, 185,
    186, -32510, 188, 207, 285, -32510, 188, 208, -32503, 188, 209, 172, -32497, 188, 168, -32494, 188, 286, -32506, 188,
    287, 218, 169, 173, 174, -32511, 188, 285, 96, -32493, 188, 226, -32505, 188, 288, 227, 228, -32450, 188, 288,
    96, -32493, 188, 226, -32510, 188, 146, -32508, 188, 227, 228, -32450, 188, 146, 96, -32489, 188, 11, -32483, 188,
    289, -32474, 188, 11, 96, -32510, 188, -32511, 57, -32510, 188, -32510, 57, 188, 290, 57, -32511, 188, -32511, 57,
    -32489, 188, 57, -32461, 188, 57, 96, -32510, 188, -32511, 57, -32510, 188, -32510, 57, 188, 291, 57, -32511, 188,
    -32511, 57, -32489, 188, 57, -32461, 188, 57, 96, -32499, 188, 292, -32508, 188, 226, -32504, 188, 227, 228, -32450,
    188, 292, 96, -32499, 188, 293, -32508, 188, 226, -32504, 188, 227, 228, -32450, 188, 293, 96, -32510, 188, -32511,
    12, -32510, 188, -32510, 12, -32511, 188, 12, -32511, 188, -32511, 12, -32489, 188, 12, -32505, 188, 294, -32470, 188,
    12, 96, -32490, 188, 295, -32510, 188, 296, -32446, 188, 296, 96, -32490, 188, 297, -32510, 188, 298, -32446, 188,
    298, 96, -32489, 188, 157, -32443, 188, 157, 96, -32493, 188, 226, -32511, 188, 299, -32510, 188, 66, -32511, 188,
    227, 228, -32450, 188, 66, 96, -32486, 188, 300, -32446, 188, 300, 96, -32479, 188, 301, -32453, 188, 301, 96,
    -32489, 188, 12, -32487, 188, 302, -32470, 188, 12, 96, -32510, 188, 59, -32509, 188, -32510, 59, -32511, 188, 59,
    -32511, 188, -32511, 59, -32489, 188, 59, -32470, 188, 303, -32505, 188, 59, 96, -32510, 188, 59, -32509, 188, -32510,
    59, -32511, 188, 59, -32511, 188, -32511, 59, -32489, 188, 59, -32470, 188, 304, -32505, 188, 59, 96, -32495, 188,
    206, -32511, 188, 185, 186, -32510, 188, 207, -32509, 188, 208, -32503, 188, 209, 172, -32497, 188, 168, -32494, 188,
    305, -32505, 188, 218, 169, 173, 174, -32511, 188, 172, 96, -32495, 188, 206, -32511, 188, 185, 186, -32510, 188,
    207, -32509, 188, 208, -32503, 188, 209, 172, -32497, 188, 168, -32494, 188, 306, -32505, 188, 218, 169, 173, 174,
    -32511, 188, 172, 96, -32510, 188, -32511, 9, -32510, 188, -32510, 9, -32511, 188, 9, -32511, 188, -32511, 9, -32489,
    188, 9, -32502, 188, 307, -32473, 188, 9, 96, -32495, 188, 206, -32511, 188, 185, 186, -32510, 188, 207, -32509,
    188, 208, -32503, 188, 209, 172, -32497, 188, 168, -32494, 188, 308, -32505, 188, 218, 169, 173, 174, -32511, 188,
    172, 96, -32489, 188, 138, -32443, 188, 138, 96, -32471, 188, 309, -32474, 188, 143, -32501, 188, 309, 96, -32489,
    188, 141, -32443, 188, 141, 96, -32495, 188, 206, -32511, 188, 185, 186, -32510, 188, 207, -32509, 188, 208, -32503,
    188, 209, 172, -32497, 188, 168, -32494, 188, 286, -32506, 188, 159, 218, 169, 173, 174, -32511, 188, 172, 96,
    -32489, 188, 158, -32443, 188, 158, 96, -32495, 188, 206, -32511, 188, 185, 186, -32510, 188, 207, -32509, 188, 208,
    -32503, 188, 209, 172, -32497, 188, 168, -32494, 188, 310, -32505, 188, 218, 169, 173, 174, -32511, 188, 172, 96,
    -32489, 188, 95, -32443, 188, 95, 96, -32510, 188, 216, -32509, 188, 238, 239, 240, -32511, 188, 241, -32511, 188,
    242, 243, -32489, 188, 244, -32503, 188, 152, -32500, 188, 126, 188, 127, 128, 129, 130, 131, 132, 133, -32506,
    188, 246, -32510, 188, 248, -32506, 188, 242, 96, -32510, 188, 216, -32509, 188, 238, 239, 240, -32511, 188, 241,
    -32511, 188, 242, 243, -32489, 188, 244, -32503, 188, 152, -32500, 188, 126, 188, 127, 128, 129, 130, 131, 132,
    133, -32506, 188, 247, -32510, 188, 249, -32506, 188, 242, 96, -32498, 188, 311, -32509, 188, 226, -32504, 188, 227,
    228, -32450, 188, 311, 96, -32498, 188, 312, -32509, 188, 226, -32504, 188, 227, 228, -32450, 188, 312, 96, -32510,
    188, 216, 313, -32510, 188, 238, 239, 240, -32511, 188, 241, -32511, 188, 242, 243, -32489, 188, 244, -32503, 188,
    102, -32500, 188, 126, 188, 127, 128, 129, 130, 131, 132, 133, -32506, 188, 246, -32510, 188, 248, -32506, 188,
    313, 96, -32493, 188, 226, -32511, 188, 46, -32510, 188, 46, -32511, 188, 227, 228, -32450, 188, 46, 96, -32490,
    188, 50, -32510, 188, 50, 314, -32447, 188, 50, 96, -32493, 188, 226, -32510, 188, 147, -32508, 188, 227, 228,
    -32450, 188, 147, 96, -32510, 188, 216, -32509, 188, 238, 239, 240, -32511, 188, 241, -32511, 188, 242, 243, -32489,
    188, 244, -32503, 188, 154, -32500, 188, 126, 188, 127, 128, 129, 130, 131, 132, 133, -32506, 188, 246, -32510,
    188, 248, -32506, 188, 242, 96, -32510, 188, 216, -32509, 188, 238, 239, 240, -32511, 188, 241, -32511, 188, 242,
    243, -32489, 188, 244, -32503, 188, 154, -32500, 188, 126, 188, 127, 128, 129, 130, 131, 132, 133, -32506, 188,
    247, -32510, 188, 249, -32506, 188, 242, 96, -32489, 188, 42, -32460, 188, 315, -32497, 188, 42, 96, -32495, 188,
    206, -32511, 188, 185, 186, -32510, 188, 207, -32509, 188, 208, -32503, 188, 209, 172, -32497, 188, 168, -32494, 188,
    316, -32505, 188, 218, 169, 173, 174, -32511, 188, 172, 96, -32489, 188, 12, -32487, 188, 317, -32470, 188, 12,
    96, -32493, 188, 226, -32505, 188, 145, 227, 228, -32450, 188, 145, 96, -32489, 188, 134, -32443, 188, 134};

    // Unwinding action table for error repair.

    private static final int[] gen_unwindingTable = 
    {10, 94, 3, 93, 5, 136, 7, 129, 136, 12, 136, 137, 16, 119, 119, 136, 97, 97, 137, 137,
    137, 83, 118, 137, 99, 99, 120, 13, 41, 121, 70, 137, 123, 118, 118, 26, 110, 3, 137, 137,
    137, 137, 137, 137, 137, 123, 20, 119, 121, 25, 120, 120, 137, 136, 118, 136, 128, 9, 105, 105,
    128, 128, 5, 69, 67, 68, 121, 136, 99, 118, 26, 138, 138, 56, 120, 137, 137, 98, 110, 110,
    137, 137, 43, 122, 45, 48, 121, 123, 118, 11, 57, 57, 108, 108, 12, 121, 121, 118, 66, 121,
    128, 12, 59, 59, 137, 137, 9, 137, 118, 136, 118, 137, 118, 137, 118, 110, 110, 109, 109, 98,
    46, 50, 118, 110, 110, 42, 137, 12, 123, 118};

    // The names of symbols.

    private static final String[] gen_symbols = 
    {
        "%%EOF",
        "const",
        "var",
        "begin",
        "end",
        "integer",
        "boolean",
        "procedure",
        "print",
        "read",
        "if",
        "then",
        "else",
        "for",
        "to",
        "do",
        "return",
        "call",
        "not",
        "and",
        "or",
        "true",
        "false",
        "comma",
        "semicolon",
        "colon",
        "lparen",
        "rparen",
        "lbracket",
        "rbracket",
        "plus",
        "minus",
        "star",
        "slash",
        "assign",
        "constEquals",
        "isEquals",
        "notEquals",
        "lessThan",
        "lessThanEquals",
        "greaterThan",
        "greaterThanEquals",
        "id",
        "intConst",
        "stringConst",
        "program",
        "Goal",
        "startMainBlock",
        "constDecList",
        "varDecList",
        "procDecList",
        "showSymbolTable",
        "StartMarker",
        "statement",
        "statementList",
        "endMainBlock",
        "constDec",
        "varDec",
        "procDec",
        "idList",
        "factor",
        "scalarType",
        "arrayIdList",
        "arrayType",
        "procHeader",
        "procBody",
        "formalList",
        "blockStmnt",
        "formal",
        "printStmnt",
        "readStmnt",
        "asgnStmnt",
        "Cond",
        "forStmnt",
        "returnStmnt",
        "callStmnt",
        "startNewBlock",
        "AddConstQuads",
        "endCurrentBlock",
        "printExprList",
        "expr",
        "inputTargetList",
        "inputTarget",
        "CondIfPart",
        "CondThenPartUM",
        "CondThenPartM",
        "CondElseJump",
        "forId",
        "exprList",
        "term",
        "prim",
        "boolConst",
        "value",
        "relop",
        "%%Goal"
    };

    // The link name for each production.

    private static final String[] gen_productionLink = 
    {
        "",
        "",
        "nonempty",
        "empty",
        "nonempty",
        "empty",
        "nonempty",
        "empty",
        "nonempty",
        "empty",
        "",
        "",
        "",
        "",
        "idList",
        "list",
        "single",
        "idList",
        "arrayIdList",
        "list",
        "single",
        "",
        "",
        "",
        "list",
        "single",
        "empty",
        "",
        "integer",
        "boolean",
        "integer",
        "boolean",
        "blockStmnt",
        "printStmnt",
        "readStmnt",
        "asgnStmnt",
        "condStmnt",
        "forStmnt",
        "returnStmnt",
        "callStmnt",
        "",
        "",
        "",
        "",
        "",
        "empty",
        "nonempty",
        "",
        "empty",
        "nonempty",
        "id",
        "idArray",
        "int",
        "intArray",
        "unmatched",
        "matched",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "nothing",
        "exprList",
        "list",
        "single",
        "plus",
        "minus",
        "or",
        "term",
        "star",
        "slash",
        "and",
        "factor",
        "positive",
        "negative",
        "not",
        "const",
        "boolConst",
        "value",
        "expr",
        "relop",
        "id",
        "expr",
        "isEquals",
        "lessThan",
        "greaterThan",
        "lessThanEquals",
        "greaterThanEquals",
        "notEquals",
        "true",
        "false",
        ""
    };




    // RLE decoding function

    private static synchronized void decodeRLE ()
    {
        if (gen_actionTable == null)
        {
            gen_actionTable = ArrayRLE.shortRLEToShort2D (rle_actionTable);
            rle_actionTable = null;
        }

        return;
    }




    // Constructor installs the generated tables into the ParserTable

    public NanoGrammarParserTable ()
    {
        super ();

        // Decode the run-length-encoded tables

        decodeRLE ();

        // Copy parsing tables into the ParserTable superclass

        _symbolCount = gen_symbolCount;
        _productionCount = gen_productionCount;
        _productionLHSSymbol = gen_productionLHSSymbol;
        _productionRHSLength = gen_productionRHSLength;
        _productionParam = gen_productionParam;
        _maxInsertion = gen_maxInsertion;
        _maxDeletion = gen_maxDeletion;
        _validationLength = gen_validationLength;
        _singlePointInsertionCount = gen_singlePointInsertionCount;
        _singlePointInsertions = gen_singlePointInsertions;
        _goalProduction = gen_goalProduction;
        _eofSymbol = gen_eofSymbol;
        _insertionCost = gen_insertionCost;
        _deletionCost = gen_deletionCost;
        _stateCount = gen_stateCount;
        _actionTable = gen_actionTable;
        _unwindingTable = gen_unwindingTable;

        // Copy dynamic-link tables into the ParserTable superclass

        _symbols = gen_symbols;
        _productionLink = gen_productionLink;

        return;
    }


}

