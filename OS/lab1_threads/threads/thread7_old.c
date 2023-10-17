#define _GNU_SOURCE

#include <unistd.h>
#include <stdio.h>
#include <string.h>
#include <sys/mman.h>
#include <fcntl.h>
#include <ucontext.h>
#include <stdlib.h>
#include <assert.h>

#define PAGE 4096
#define STACK_SIZE (PAGE * 8)
#define MAX_THREADS_COUNT 8


typedef struct {
    int uthread_id;

    void (*start_routine)(void *);

    void *arg;
    ucontext_t ucontext;
} uthread_t;

uthread_t *uthreads[MAX_THREADS_COUNT];
int uthread_count = 0;
int uthread_cur = 0;

void *create_stack(off_t size, int thread_num) {
    char stack_file[128];
    int stack_fd;
    void *stack;
    snprintf(stack_file, sizeof(stack_file), "stack-%d", thread_num);

    stack_fd = open(stack_file, O_RDWR | O_CREAT, 0660);
    if (stack_fd == -1) {
        printf("create_stack: failed to open file");
        return NULL;
    }

    int err = ftruncate(stack_fd, 0);
    if (err == -1) {
        printf("create_stack: failed to ftruncate");
        return NULL;
    }

    err = ftruncate(stack_fd, size);
    if (err == -1) {
        printf("create_stack: failed to ftruncate");
        return NULL;
    }

    stack = mmap(NULL, size, PROT_READ | PROT_WRITE, MAP_SHARED, stack_fd, 0);
    if (stack == MAP_FAILED) {
        printf("create_stack: failed mmap");
        return NULL;
    }

    err = close(stack_fd);
    if (err == -1) {
        printf("create_stack: failed mmap");
        return NULL;
    }

    memset(stack, 0x7f, size);
    return stack;
}


void uthread_scheduler(void) {
    int err;
    ucontext_t *cur_context, *next_context;

    cur_context = &(uthreads[uthread_cur]->ucontext);
    uthread_cur = (uthread_cur + 1) % uthread_count;
    next_context = &(uthreads[uthread_cur]->ucontext);

    err = swapcontext(cur_context, next_context);

    if (err == -1) {
        printf("Bad swap context");
        exit(1);
    }
}

void uthread_startup(void *arg) {
    uthread_t *ut = (uthread_t *) arg;
    ut->start_routine(ut->arg);
}


int uthread_create(uthread_t **ut, void(*thread_func)(void *), void *arg) {
    char *stack;
    uthread_t *new_ut;
    int err;

    stack = create_stack(STACK_SIZE, uthread_count);
    if (stack == NULL) {
        printf("uthread_create: failed to create stack");
        return -1;
    }

    new_ut = (uthread_t *) (stack + STACK_SIZE - sizeof(uthread_t));

    err = getcontext(&new_ut->ucontext);
    if (err == -1) {
        printf("Bad getcontext in thread %d", uthread_count);
        return -1;
    }
    new_ut->ucontext.uc_stack.ss_sp = stack;
    new_ut->ucontext.uc_stack.ss_size = STACK_SIZE - sizeof(uthread_t);
    new_ut->ucontext.uc_link = NULL;
    makecontext(&new_ut->ucontext, (void (*)(void)) uthread_startup, 1, new_ut);

    new_ut->start_routine = thread_func;
    new_ut->arg = arg;
    new_ut->uthread_id = uthread_count;

    uthreads[uthread_count] = new_ut;
    uthread_count++;
    *ut = new_ut;
    return 1;

}

void mythread_func_1(void *arg) {
    char *str = (char *) arg;
    printf("mythread_func_1 started,arg: %s, pid %d, ppid %d, tid %d", str, getpid(), getppid(), gettid());
    for (int i = 0; i < 10; i++) {
        printf("hello in mythread_func_1: %s\n", str);
        sleep(1);
        uthread_scheduler();
    }
    puts("mythread_func_1 finished");
}

void mythread_func_2(void *arg) {
    char *str = (char *) arg;
    printf("mythread_func_2 started,arg: %s, pid %d, ppid %d, tid %d", str, getpid(), getppid(), gettid());
    for (int i = 0; i < 10; i++) {
        printf("hello in mythread_func_2: %s\n", str);
        sleep(1);
        uthread_scheduler();
    }
    puts("mythread_func_2 finished");
}

void mythread_func_3(void *arg) {
    char *str = (char *) arg;
    printf("mythread_func_3 started,arg: %s, pid %d, ppid %d, tid %d", str, getpid(), getppid(), gettid());
    for (int i = 0; i < 10; i++) {
        printf("hello in mythread_func_3: %s\n", str);
        sleep(1);
        uthread_scheduler();
    }
    puts("mythread_func_3 finished");
}

int main() {
    uthread_t *ut[3];
    uthread_t main_thread;
    uthreads[0] = &main_thread;
    uthread_count = 1;
    printf("main [%d %d %d]: Hello from main!\n", getpid(), getppid(), gettid());

    int err = uthread_create(&ut[0], mythread_func_1, "Arg hello 1");
    if (err == -1) {
        puts("Create 1 failed");
        exit(1);
    }

    err = uthread_create(&ut[1], mythread_func_2, "Arg hello 2");
    if (err == -1) {
        puts("Create 2 failed");
        exit(1);
    }

    err = uthread_create(&ut[2], mythread_func_3, "Arg hello 3");
    if (err == -1) {
        puts("Create 3 failed");
        exit(1);
    }

    while (1) {
        uthread_scheduler();
    }
    puts("main finished");
    return 0;
}
