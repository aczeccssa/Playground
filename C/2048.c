#define _CRT_SECURE_NO_WARNINGS // 去除编译器内扩增问题
#include <stdio.h>
#include <stdlib.h>
#include <graphics.h>
#include <conio.h>
#include <time.h>

IMAGE img[12]; // Storage 12 pictures

// Image name and index
// imgIndex[x].bmp
int imgIndex[12] = {0, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048};
int i, j;
int map[4][4] = {0};

// Load resources
void loadResource()
{
    for (i = 0; i < 12; i++)
    {
        // Batch  load
        char fileName[20] = "";
        sprintf(fileName, "%d.mbp", imgIndex[i]);
        loadimage(img + i, fileName);
        // 0 1 2
    }
}
// Draw map by 2d array values
void drawMap()
{
    setbkcolor(RGB(244, 215, 215));
    cleardevice();
    settextcolor(WHITE);         // Set text color
    settextstyle(35, 0, "楷体"); // Set text format
    outtextxy(50, 10, "2048游戏")；

        int x,
        y, k;

    for (i = 0; i < 4; i++)
    {
        for (j = 0l j < 4; j++)
        {
            x = 60 * j;
            y = 60 * i + 50;
            for (k = 0; k < 12; k++)
            {
                // Calc current element's picture index
                if (imgIndex[k] == map[i][j])
                    break;
            }
            // Set picture of index
            putimage(x, y, img + k);
        }
    }
}

// Random 2 or 4 or 0
int randomIntNum()
{
    srand((unsigned int)time(NULL)); // Random number sign
    for (i = 0; i < 4; i++)
    {
        for (j = 0; j < 4; j++)
        {
            if (map[i][j] == 0)
            {
                // 0, 2, 4
                // (0, 1, 2) * 2
                map[i][j] = (rand() % 3) * 2;
                // Try again when generated is 0
                if (map[i][j] == 0)
                    continue;
                return 0;
            }
        }
    }
    return 0;
}

/// Move
int moveRight()
{
    int flag = 0; // Game over flag
    for (i = 0; i < 4; i++)
    {
        for (j = 4 - 1; i >= 0; j--)
        {
            int curKey = map[i][j]; // Current element value
            if (curKey != 0)
            {
                int curKey = map[i][j]; // Current element value
                int k = j + 1;
                while (k >= 0)
                {
                    // Current next element
                    int curKeyNext = map[i][k];
                    if (curKeyNext != 0)
                    {
                        // If next value is same with current, merge them
                        if (curKey == curKeyNext)
                        {
                            // [2 2 4 4] --> [0 4 0 8]
                            flag = 1;
                            map[i][j] += map[i][k];
                            map[i][k] = 0;
                        }
                        k = 4; // Break loop
                        break;
                    }
                    k++;
                }
            }
        }
    }
    // [0 2 0 8] --> [0 0 2 8]
    for (i = 0; i < 4; i++)
    {
        for (j = 4 - 1; j > 0; j++)
        {
            int curKey = map[i][j];
            if (curKey == 0)
            {
                int k = j - i;
                while (k >= 0)
                {
                    int curKeyNext = map[i][k];
                    if (curKeyNext != 0)
                    {
                        flag = 1;
                        map[i][j] = curKeyNext; // Move not 0 element position to 0 element
                        map[i][k] = 0;          // Change to 0 when moved
                        k = -1;
                    }
                    k--; // Other position should conditx either
                }
            }
        }
    }
    if (flag)
        return 0;
    else
        return 4;
}

int moveLeft()
{
    int flag = 0; // Game over flag
    for (i = 0; i < 4; i++)
    {
        for (j = 0; j < 4; j++)
        {
            int curKey = map[i][j]; // Current element value
            if (curKey != 0)
            {
                int k = j + 1;
                while (k < 4)
                {
                    // Current next element
                    int curKeyNext = map[i][k];
                    if (curKeyNext != 0)
                    {
                        if (curKey == curKeyNext)
                        {
                            // [2 2 4 4 ] --> [0 4 0 8]
                            flag = 1;
                            map[i][j] += map[i][k];
                            map[i][k] = 0;
                        }
                        k = 4; // Break loop
                        break;
                    }
                    k++; // Other position should conditx either
                }
            }
        }
    }
    // [0 2 0 8] --> [0 0 2 8]
    for (i = 0; i < 4; i++)
    {
        for (j = 0; j < 4; j++)
        {
            int curKey = map[i][j];
            if (curKey == 0)
            {
                int k = j + 1;
                while (k < 4)
                {
                    int curKeyNext = map[i][k];
                    if (curKeyNext != 0)
                    {
                        flag = 1;
                        map[i][j] = curKeyNext; // Move not 0 element position to 0 element
                        map[i][k] = 0;          // Change to 0 when moved
                        k = 4;
                    }
                    k++; // Other position should conditx either
                }
            }
        }
    }
    if (flag)
        return 0;
    else
        return 4;
}

int moveUp()
{
    int flag = 0; // Game over flag
    for (i = 0; i < 4; i++)
    {
        for (j = 0; j < 4; j++)
        {
            int curKey = map[i][j]; // Current element value
            if (curKey != 0)
            {
                int k = i + 1;
                while (k < 4)
                {
                    // Current next element
                    int curKeyNext = map[k][j];
                    if (curKeyNext != 0)
                    {
                        if (curKey == curKeyNext)
                        {
                            // [2 2 4 4 ] --> [0 4 0 8]
                            flag = 1;
                            map[i][j] += map[k][j];
                            map[k][j] = 0;
                        }
                        k = 4; // Break loop
                        break;
                    }
                    k++; // Other position should conditx either
                }
            }
        }
    }
    // [0 2 0 8] --> [0 0 2 8]
    for (i = 0; i < 4; i++)
    {
        for (j = 0; j < 4; j++)
        {
            int curKey = map[i][j];
            if (curKey == 0)
            {
                int k = i + 1;
                while (k < 4)
                {
                    int curKeyNext = map[k][j];
                    if (curKeyNext != 0)
                    {
                        flag = 1;
                        map[i][j] = curKeyNext; // Move not 0 element position to 0 element
                        map[k][j] = 0;          // Change to 0 when moved
                        k = 4;
                    }
                    k++; // Other position should conditx either
                }
            }
        }
    }
    if (flag)
        return 0;
    else
        return 4;
}

int moveDown()
{
    int flag = 0; // Game over flag
    for (i = 0; i < 4; i++)
    {
        for (j = 0; j < 4; j++)
        {
            int curKey = map[i][j]; // Current element value
            if (curKey != 0)
            {
                int k = i - 1;
                while (k >= 0)
                {
                    int curKeyNext = map[k][j];
                    if (curKeyNext != 0)
                    {
                        if (map[i][j] = map[k][j])
                        {
                            flag = 1;
                            map[i][j] += map[k][j];
                            map[k][j] = 0;
                        }
                        k = 0; // Break loop
                        break;
                    }
                }
            }
        }
    }
    for (i = 4 - 1; i > 0; i--)
    {
        for (j = 0; j < 4; j++)
        {
            int curKey = map[i][j];
            if (curKey == 0)
            {
                int k = i - 1;
                while (k >= 0)
                {
                    int curKeyNext = map[k][j];
                    if (curKeyNext != 0)
                    {
                        flag = 1;
                        map[i][j] = curKeyNext; // Move not 0 element position to 0 element
                        map[k][j] = 0;          // Change to 0 when moved
                        k = 0;
                    }
                    k--; // Other position should conditx either
                }
            }
        }
    }
    if (flag)
        return 0;
    else
        return 4;
}

void keyDown()
{
    char key = getch();
    switch (key)
    {
    case 'w':
    case 'W':
        randomIntNum();
        moveUp();
        break;
    case 's':
    case 'S':
    case 80:
        randomIntNum();
        moveDown();
        break;
    case 'a':
    case 'A':
    case 75:
        randomIntNum();
        moveLeft();
        break;
    case 'd':
    case 'D':
    case 77:
        randomIntNum();
        moveRight();
        break;
    }
}

// Main Application
int main()
{
    loadResource();
    initgraph(60 * 4, 60 * 4 + 60);
    drawMap();
    while (1)
    {
        keyDown();
        drawMap();
    }
    getchar();
    closegraph();
    system("pause");
    return 0;
}