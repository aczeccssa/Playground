#include <stdio.h>
#include <stdlib.h>
#include <string.h>

// Defined a struct to store employee information
typedef struct
{
    int id;
    char name[20];
    float salary;
} Employee;

// Defined a function pointer type for print function
typedef void (*PrintFuncPtr)(Employee *);

// Defined a function pointer type for modify function
typedef void (*ModifyFuncPtr)(Employee *, int, char *, float);

// Defined a function to print employee information
void printEmployee(Employee *emp) {
    printf("ID: %d, Name: %s, Salary: %.2f\n", emp->id, emp->name, emp->salary);
}

// Defined a function to modify employee information
void modifyEmployee(Employee *emp, int id, char *name, float salary) {
    emp->id = id;
    strcpy(emp->name, name);
    emp->salary = salary;
}

int main() {
    // Created an array of employees
    Employee employees[3] = {
        {1, "Alice", 5000.0},
        {2, "Bob", 6000.0},
        {3, "Charlie", 7000.0}};

    // Defined a function pointer array with the correct types
    PrintFuncPtr printFunc = printEmployee;
    ModifyFuncPtr modifyFunc = modifyEmployee;

    // Call the functions using the function pointers
    for (int i = 0; i < 3; i++) {
        printFunc(&employees[i]);
        modifyFunc(&employees[i], i + 1, "New Name", 8000.0);
        printFunc(&employees[i]);
    }

    return 0;
}