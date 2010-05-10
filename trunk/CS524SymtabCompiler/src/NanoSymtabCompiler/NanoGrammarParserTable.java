// File generated by Invisible Jacc version 1.1.
// Invisible Jacc is Copyright 1997-1998 Invisible Software, Inc.

package NanoSymtabCompiler;

import invisible.jacc.parse.ParserTable;
import invisible.jacc.util.ArrayRLE;

public class NanoGrammarParserTable extends ParserTable
{

    // The number of symbols.

    private static final int gen_symbolCount = 85;

    // The number of productions.

    private static final int gen_productionCount = 85;

    // The symbol on the left hand side of each production.

    private static final int[] gen_productionLHSSymbol = 
    {45, 44, 47, 47, 48, 48, 49, 49, 52, 52, 46, 53, 50, 54, 57, 57, 55, 55, 60, 60,
    56, 62, 62, 62, 64, 59, 59, 61, 61, 51, 51, 51, 51, 51, 51, 51, 51, 63, 72, 73,
    65, 74, 74, 66, 76, 76, 77, 77, 67, 67, 68, 68, 69, 70, 71, 71, 78, 78, 75, 75,
    75, 75, 79, 79, 79, 79, 58, 58, 58, 80, 80, 80, 80, 80, 82, 82, 83, 83, 83, 83,
    83, 83, 81, 81, 84};

    // The length of the right hand side of each production.

    private static final int[] gen_productionRHSLength = 
    {1, 12, 2, 0, 2, 0, 2, 0, 2, 0, 0, 0, 0, 5, 3, 1, 5, 5, 6, 4,
    7, 3, 1, 0, 3, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 10, 0, 0,
    6, 0, 3, 6, 0, 3, 1, 4, 4, 7, 4, 6, 8, 2, 5, 6, 3, 1, 3, 3,
    3, 1, 3, 3, 3, 1, 1, 2, 2, 1, 1, 1, 3, 5, 1, 4, 1, 1, 1, 1,
    1, 1, 1, 1, 2};

    // The parameter for each production.

    private static final int[] gen_productionParam = 
    {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    0, 0, 0, 0, 0};

    // The maximum number of insertions during error repair.

    private static final int gen_maxInsertion = 100;

    // The maximum number of deletions during error repair.

    private static final int gen_maxDeletion = 200;

    // The validation length for error repair.

    private static final int gen_validationLength = 5;

    // The number of single-point insertions for error repair.

    private static final int gen_singlePointInsertionCount = 43;

    // The list of symbols for single-point insertions.

    private static final int[] gen_singlePointInsertions = 
    {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20,
    21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40,
    41, 42, 43};

    // The goal production.

    private static final int gen_goalProduction = 84;

    // The end-of-file symbol.

    private static final int gen_eofSymbol = 0;

    // Insertion cost of each symbol for error repair.

    private static final int[] gen_insertionCost = 
    {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
    1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
    1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    0, 0, 0, 0, 0};

    // Deletion cost of each symbol for error repair.

    private static final int[] gen_deletionCost = 
    {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
    1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
    1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    0, 0, 0, 0, 0};

    // The number of LR(1) states.

    private static final int gen_stateCount = 128;

    // Parsing action table.

    private static short[][] gen_actionTable = null;
    private static short[] rle_actionTable = 
    {128, 86, 170, -32510, 10, -32510, 170, 10, -32477, 170, 85, 171, 172, -32475, 170, 10, 86, 84, -32429, 170,
    173, 86, 170, -32510, 3, -32510, 170, 3, -32474, 170, 174, -32476, 170, 3, 86, -32428, 170, 84, 86, 170,
    175, -32511, 5, -32510, 170, 5, -32473, 170, 176, -32508, 170, 87, -32483, 170, 5, 86, -32472, 170, 177, -32498,
    170, 178, -32486, 170, 177, 86, -32511, 170, 179, 7, -32510, 170, 7, -32472, 170, 180, -32508, 170, 89, -32484,
    170, 7, 86, -32490, 170, 181, 170, 15, -32504, 170, 15, -32464, 170, 15, 86, -32478, 170, 182, -32464, 170,
    182, 86, -32472, 170, 183, -32498, 170, 184, -32511, 170, 185, -32489, 170, 183, 86, -32510, 170, 12, -32510, 170,
    186, -32471, 170, 187, -32508, 170, 91, -32485, 170, 12, 86, -32472, 170, 177, -32498, 170, 99, -32486, 170, 177,
    86, -32495, 170, 188, -32511, 170, 167, 168, -32510, 170, 189, -32509, 170, 190, -32504, 170, 191, 154, -32498, 170,
    192, -32492, 170, 151, 155, 156, -32511, 170, 154, 86, -32490, 170, 181, 170, 15, -32511, 170, 193, -32457, 170,
    15, 86, -32488, 170, 194, -32454, 170, 194, 86, -32488, 170, 195, -32454, 170, 195, 86, -32472, 170, 196, -32470,
    170, 196, 86, -32510, 170, 197, -32432, 170, 197, 86, -32492, 170, 167, 168, -32510, 170, 189, -32499, 170, 191,
    154, -32476, 170, 153, 155, 156, -32511, 170, 154, 86, -32495, 170, 188, -32511, 170, 167, 168, -32510, 170, 189,
    -32509, 170, 190, -32504, 170, 191, 154, -32498, 170, 150, -32497, 170, 198, -32510, 170, 199, 151, 155, 156, -32511,
    170, 154, 86, -32492, 170, 167, 168, -32510, 170, 189, -32499, 170, 191, 154, -32476, 170, 152, 155, 156, -32511,
    170, 154, 86, -32502, 170, 74, -32511, 170, -32511, 74, -32510, 170, -32511, 74, -32511, 170, -32511, 74, -32511, 170,
    74, 200, -32508, 74, 170, -32507, 74, -32469, 170, 74, 86, -32489, 170, 98, -32453, 170, 98, 86, -32471, 170,
    201, -32471, 170, 201, 86, -32508, 170, 110, 111, -32461, 170, 202, -32488, 170, 110, 86, -32508, 170, 112, 113,
    -32459, 170, 203, -32490, 170, 112, 86, -32487, 170, 204, -32455, 170, 204, 86, -32510, 170, 205, -32509, 170, 206,
    207, 208, -32511, 170, 210, -32511, 170, 212, 213, -32490, 170, 214, -32504, 170, 215, -32502, 170, 114, 170, 115,
    116, 117, 118, 119, 120, 121, -32500, 170, 212, 86, -32493, 170, 216, -32507, 170, 157, -32511, 170, 217, 218,
    -32510, 170, 161, 166, 162, 164, 163, 165, -32471, 170, 219, 170, 157, 86, -32502, 170, 61, -32511, 170, -32511,
    61, -32510, 170, 220, 61, -32511, 170, -32511, 61, -32511, 170, 61, 170, -32510, 61, 221, 222, 170, -32507, 61,
    -32469, 170, 61, 86, -32495, 170, 188, -32511, 170, 167, 168, -32510, 170, 189, -32509, 170, 190, -32504, 170, 191,
    154, -32498, 170, 150, -32497, 170, 223, -32510, 170, 199, 151, 155, 156, -32511, 170, 154, 86, -32484, 170, 224,
    -32458, 170, 224, 86, -32489, 170, 101, -32453, 170, 101, 86, -32489, 170, 102, -32453, 170, 102, 86, -32486, 170,
    23, -32500, 170, 177, -32498, 170, 225, -32509, 170, 226, 170, 227, -32493, 170, 23, 86, 170, -32509, 38, -32510,
    170, -32510, 38, -32511, 170, 38, -32511, 170, -32511, 38, -32490, 170, 38, -32483, 170, 228, -32501, 170, 38, 86,
    -32487, 170, 229, -32455, 170, 229, 86, -32487, 170, 230, -32455, 170, 230, 86, -32495, 170, 188, -32511, 170, 167,
    168, -32510, 170, 189, -32509, 170, 190, -32504, 170, 191, 154, -32498, 170, 150, -32497, 170, 231, -32510, 170, 199,
    151, 155, 156, -32511, 170, 154, 86, -32495, 170, 188, -32511, 170, 167, 168, -32510, 170, 189, -32509, 170, 190,
    -32504, 170, 191, 154, -32498, 170, 150, -32497, 170, 232, -32510, 170, 199, 151, 155, 156, -32511, 170, 154, 86,
    -32472, 170, 233, -32470, 170, 233, 86, -32472, 170, 234, -32470, 170, 234, 86, -32489, 170, 138, -32453, 170, 138,
    86, -32472, 170, 235, -32470, 170, 235, 86, -32485, 170, 236, -32508, 170, 237, -32463, 170, 237, 86, -32510, 170,
    -32511, 9, -32510, 170, -32510, 9, -32511, 170, 9, -32511, 170, -32511, 9, -32490, 170, 9, -32503, 170, 238, -32481,
    170, 9, 86, -32495, 170, 188, -32511, 170, 167, 168, -32510, 170, 189, -32509, 170, 190, -32504, 170, 191, 154,
    -32498, 170, 150, -32493, 170, 239, 151, 155, 156, -32511, 170, 154, 86, -32495, 170, 188, -32511, 170, 167, 168,
    -32510, 170, 189, -32509, 170, 190, -32504, 170, 191, 154, -32498, 170, 150, -32493, 170, 240, 151, 155, 156, -32511,
    170, 154, 86, -32495, 170, 188, -32511, 170, 167, 168, -32510, 170, 189, -32509, 170, 190, -32504, 170, 191, 154,
    -32498, 170, 150, -32493, 170, 241, 151, 155, 156, -32511, 170, 154, 86, -32495, 170, 188, -32511, 170, 167, 168,
    -32510, 170, 189, -32509, 170, 190, -32504, 170, 191, 154, -32498, 170, 150, -32497, 170, 242, -32510, 170, 199, 151,
    155, 156, -32511, 170, 154, 86, -32495, 170, 188, -32511, 170, 167, 168, -32510, 170, 189, -32509, 170, 190, -32504,
    170, 191, 154, -32498, 170, 149, -32492, 170, 151, 155, 156, -32511, 170, 154, 86, -32495, 170, 188, -32511, 170,
    167, 168, -32510, 170, 189, -32509, 170, 190, -32504, 170, 191, 154, -32498, 170, 147, -32492, 170, 151, 155, 156,
    -32511, 170, 154, 86, -32495, 170, 188, -32511, 170, 167, 168, -32510, 170, 189, -32509, 170, 190, -32504, 170, 191,
    154, -32498, 170, 148, -32492, 170, 151, 155, 156, -32511, 170, 154, 86, -32493, 170, 216, -32505, 170, 160, 217,
    218, -32460, 170, 160, 86, -32490, 170, 243, 170, 19, -32454, 170, 19, 86, -32488, 170, 244, -32454, 170, 244,
    86, -32486, 170, 245, -32456, 170, 245, 86, -32489, 170, 246, -32511, 170, 22, -32456, 170, 22, 86, 170, -32509,
    3, -32510, 170, -32510, 3, -32511, 170, 3, -32511, 170, -32511, 3, -32490, 170, 3, -32508, 170, 247, -32476, 170,
    3, 86, -32470, 170, 248, -32472, 170, 248, 86, -32470, 170, 249, -32472, 170, 249, 86, -32502, 170, 250, -32505,
    170, 216, -32504, 170, 217, 218, -32460, 170, 250, 86, -32502, 170, 251, -32505, 170, 216, -32504, 170, 217, 218,
    -32460, 170, 251, 86, -32479, 170, 252, -32463, 170, 252, 86, -32479, 170, 253, -32463, 170, 253, 86, -32487, 170,
    254, -32455, 170, 254, 86, -32495, 170, 188, -32511, 170, 167, 168, -32510, 170, 189, -32509, 170, 190, -32504, 170,
    191, 154, -32498, 170, 150, -32497, 170, 255, -32510, 170, 199, 151, 155, 156, -32511, 170, 154, 86, -32495, 170,
    188, -32511, 170, 167, 168, -32510, 170, 189, -32509, 170, 190, -32504, 170, 191, 154, -32498, 170, 150, -32497, 170,
    256, -32510, 170, 199, 151, 155, 156, -32511, 170, 154, 86, -32510, 170, 205, 257, -32510, 170, 206, 207, 208,
    -32511, 170, 210, -32511, 170, 212, 213, -32490, 170, 214, -32504, 170, 93, -32502, 170, 114, 170, 115, 116, 117,
    118, 119, 120, 121, -32500, 170, 257, 86, -32502, 170, 60, -32511, 170, -32511, 60, -32510, 170, 220, 60, -32511,
    170, -32511, 60, -32511, 170, 60, 170, -32510, 60, 221, 222, 170, -32507, 60, -32469, 170, 60, 86, -32502, 170,
    58, -32511, 170, -32511, 58, -32510, 170, 220, 58, -32511, 170, -32511, 58, -32511, 170, 58, 170, -32510, 58, 221,
    222, 170, -32507, 58, -32469, 170, 58, 86, -32502, 170, 59, -32511, 170, -32511, 59, -32510, 170, 220, 59, -32511,
    170, -32511, 59, -32511, 170, 59, 170, -32510, 59, 221, 222, 170, -32507, 59, -32469, 170, 59, 86, -32493, 170,
    216, -32507, 170, 158, -32511, 170, 217, 218, -32460, 170, 158, 86, -32472, 170, 258, -32495, 170, 103, -32489, 170,
    258, 86, -32508, 170, 110, 111, -32461, 170, 109, -32488, 170, 110, 86, -32489, 170, 259, -32453, 170, 259, 86,
    -32486, 170, 23, -32500, 170, 177, -32498, 170, 225, -32509, 170, 106, 170, 227, -32493, 170, 23, 86, 170, 175,
    -32510, 5, -32510, 170, -32510, 5, -32511, 170, 5, -32511, 170, -32511, 5, -32490, 170, 5, -32507, 170, 260, -32508,
    170, 87, -32483, 170, 5, 86, -32490, 170, 41, -32510, 170, 41, -32467, 170, 261, -32503, 170, 41, 86, -32490,
    170, 44, -32510, 170, 44, -32465, 170, 262, -32505, 170, 44, 86, -32510, 170, 205, -32509, 170, 206, 207, 209,
    -32511, 170, 211, -32511, 170, 212, 213, -32490, 170, 214, -32504, 170, 263, -32502, 170, 114, 170, 115, 116, 117,
    118, 119, 120, 121, -32500, 170, 212, 86, -32510, 170, 205, -32509, 170, 206, 207, 209, -32511, 170, 211, -32511,
    170, 212, 213, -32490, 170, 214, -32504, 170, 264, -32502, 170, 114, 170, 115, 116, 117, 118, 119, 120, 121,
    -32500, 170, 212, 86, -32495, 170, 188, -32511, 170, 167, 168, -32510, 170, 189, -32509, 170, 190, -32504, 170, 191,
    154, -32498, 170, 150, -32497, 170, 265, -32510, 170, 199, 151, 155, 156, -32511, 170, 154, 86, -32495, 170, 188,
    -32511, 170, 167, 168, -32510, 170, 189, -32509, 170, 190, -32504, 170, 191, 154, -32498, 170, 150, -32497, 170, 266,
    -32510, 170, 199, 151, 155, 156, -32511, 170, 154, 86, -32495, 170, 188, -32511, 170, 167, 168, -32510, 170, 189,
    267, -32510, 170, 190, -32504, 170, 191, 154, -32498, 170, 150, -32497, 170, 268, -32511, 170, 269, 199, 151, 155,
    156, -32511, 170, 267, 86, -32493, 170, 216, -32505, 170, 270, 217, 218, -32460, 170, 270, 86, -32493, 170, 216,
    -32510, 170, 133, -32508, 170, 217, 218, -32460, 170, 133, 86, -32489, 170, 11, -32485, 170, 271, -32482, 170, 11,
    86, -32485, 170, 193, -32457, 170, 193, 86, -32510, 170, 205, -32454, 170, 105, -32492, 170, 205, 86, -32511, 170,
    179, -32511, 12, -32510, 170, -32510, 12, -32511, 170, 12, -32511, 170, -32511, 12, -32490, 170, 12, -32505, 170, 272,
    -32509, 170, 89, -32484, 170, 12, 86, -32490, 170, 273, -32510, 170, 274, -32456, 170, 274, 86, -32490, 170, 275,
    -32510, 170, 276, -32456, 170, 276, 86, -32510, 170, -32511, 50, -32510, 170, -32510, 50, 170, 277, 50, -32511, 170,
    -32511, 50, -32490, 170, 50, -32470, 170, 50, 86, -32510, 170, -32511, 50, -32510, 170, -32510, 50, 170, 278, 50,
    -32511, 170, -32511, 50, -32490, 170, 50, -32470, 170, 50, 86, -32499, 170, 279, -32508, 170, 216, -32504, 170, 217,
    218, -32460, 170, 279, 86, -32499, 170, 280, -32508, 170, 216, -32504, 170, 217, 218, -32460, 170, 280, 86, -32489,
    170, 139, -32453, 170, 139, 86, -32493, 170, 216, -32511, 170, 281, -32510, 170, 57, -32511, 170, 217, 218, -32460,
    170, 57, 86, -32486, 170, 282, -32456, 170, 282, 86, -32479, 170, 283, -32463, 170, 283, 86, -32489, 170, 12,
    -32488, 170, 284, -32479, 170, 12, 86, -32510, 170, -32511, 9, -32510, 170, -32510, 9, -32511, 170, 9, -32511, 170,
    -32511, 9, -32490, 170, 9, -32503, 170, 285, -32481, 170, 9, 86, -32495, 170, 188, -32511, 170, 167, 168, -32510,
    170, 189, -32509, 170, 190, -32504, 170, 191, 154, -32498, 170, 150, -32497, 170, 286, -32510, 170, 199, 151, 155,
    156, -32511, 170, 154, 86, -32489, 170, 125, -32453, 170, 125, 86, -32472, 170, 287, -32478, 170, 130, -32506, 170,
    287, 86, -32489, 170, 128, -32453, 170, 128, 86, -32510, 170, 205, -32509, 170, 206, 207, 208, -32511, 170, 210,
    -32511, 170, 212, 213, -32490, 170, 214, -32504, 170, 136, -32502, 170, 114, 170, 115, 116, 117, 118, 119, 120,
    121, -32500, 170, 212, 86, -32510, 170, 205, -32509, 170, 206, 207, 209, -32511, 170, 211, -32511, 170, 212, 213,
    -32490, 170, 214, -32504, 170, 136, -32502, 170, 114, 170, 115, 116, 117, 118, 119, 120, 121, -32500, 170, 212,
    86, -32495, 170, 188, -32511, 170, 167, 168, -32510, 170, 189, -32509, 170, 190, -32504, 170, 191, 154, -32498, 170,
    150, -32497, 170, 288, -32510, 170, 199, 151, 155, 156, -32511, 170, 154, 86, -32495, 170, 188, -32511, 170, 167,
    168, -32510, 170, 189, -32509, 170, 190, -32504, 170, 191, 154, -32498, 170, 150, -32497, 170, 289, -32510, 170, 199,
    151, 155, 156, -32511, 170, 154, 86, -32495, 170, 188, -32511, 170, 167, 168, -32510, 170, 189, -32509, 170, 190,
    -32504, 170, 191, 154, -32498, 170, 150, -32497, 170, 268, -32511, 170, 141, 199, 151, 155, 156, -32511, 170, 154,
    86, -32489, 170, 140, -32453, 170, 140, 86, -32495, 170, 188, -32511, 170, 167, 168, -32510, 170, 189, -32509, 170,
    190, -32504, 170, 191, 154, -32498, 170, 150, -32497, 170, 290, -32510, 170, 199, 151, 155, 156, -32511, 170, 154,
    86, -32489, 170, 86, -32453, 170, 86, 86, -32510, 170, 205, 291, -32510, 170, 206, 207, 208, -32511, 170, 210,
    -32511, 170, 212, 213, -32490, 170, 214, -32504, 170, 93, -32502, 170, 114, 170, 115, 116, 117, 118, 119, 120,
    121, -32500, 170, 291, 86, -32493, 170, 216, -32511, 170, 42, -32510, 170, 42, -32511, 170, 217, 218, -32460, 170,
    42, 86, -32490, 170, 46, -32510, 170, 46, 292, -32457, 170, 46, 86, -32498, 170, 293, -32509, 170, 216, -32504,
    170, 217, 218, -32460, 170, 293, 86, -32498, 170, 294, -32509, 170, 216, -32504, 170, 217, 218, -32460, 170, 294,
    86, -32493, 170, 216, -32510, 170, 134, -32508, 170, 217, 218, -32460, 170, 134, 86, -32489, 170, 39, -32465, 170,
    295, -32502, 170, 39, 86, -32495, 170, 188, -32511, 170, 167, 168, -32510, 170, 189, -32509, 170, 190, -32504, 170,
    191, 154, -32498, 170, 150, -32497, 170, 296, -32510, 170, 199, 151, 155, 156, -32511, 170, 154, 86, -32510, 170,
    205, -32509, 170, 206, 207, 208, -32511, 170, 210, -32511, 170, 212, 213, -32490, 170, 214, -32504, 170, 137, -32502,
    170, 114, 170, 115, 116, 117, 118, 119, 120, 121, -32500, 170, 212, 86, -32510, 170, 205, -32509, 170, 206,
    207, 209, -32511, 170, 211, -32511, 170, 212, 213, -32490, 170, 214, -32504, 170, 137, -32502, 170, 114, 170, 115,
    116, 117, 118, 119, 120, 121, -32500, 170, 212, 86, -32489, 170, 12, -32488, 170, 297, -32479, 170, 12, 86,
    -32493, 170, 216, -32505, 170, 132, 217, 218, -32460, 170, 132, 86, -32489, 170, 122, -32453, 170, 122};

    // Unwinding action table for error repair.

    private static final int[] gen_unwindingTable = 
    {10, 85, 3, 84, 5, 126, 7, 15, 120, 126, 12, 126, 127, 15, 110, 110, 126, 88, 127, 127,
    127, 74, 109, 127, 90, 90, 111, 101, 112, 61, 127, 114, 109, 109, 23, 38, 111, 111, 127, 127,
    126, 126, 109, 126, 119, 9, 127, 127, 127, 127, 127, 127, 127, 114, 19, 110, 112, 22, 3, 128,
    128, 96, 96, 119, 119, 111, 127, 127, 89, 60, 58, 59, 112, 126, 90, 109, 23, 5, 41, 44,
    101, 101, 127, 127, 112, 114, 109, 11, 113, 88, 12, 112, 112, 50, 50, 99, 99, 109, 57, 112,
    119, 12, 9, 127, 109, 126, 109, 101, 101, 127, 127, 127, 109, 127, 109, 89, 42, 46, 100, 100,
    109, 39, 127, 101, 101, 12, 114, 109};

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
        "equals",
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
        "formalList",
        "blockStmnt",
        "formal",
        "printStmnt",
        "readStmnt",
        "asgnStmnt",
        "condStmnt",
        "forStmnt",
        "returnStmnt",
        "callStmnt",
        "startNewBlock",
        "endCurrentBlock",
        "printExprList",
        "expr",
        "inputTargetList",
        "inputTarget",
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
        "idList",
        "list",
        "single",
        "idList",
        "arrayIdList",
        "list",
        "single",
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
        "empty",
        "nonempty",
        "",
        "empty",
        "nonempty",
        "id",
        "idArray",
        "int",
        "intArray",
        "ifThen",
        "ifThenElse",
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
        "equals",
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

