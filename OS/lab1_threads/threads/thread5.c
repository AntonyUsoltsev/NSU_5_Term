#define _GNU_SOURCE

#include <stdio.h>
#include <pthread.h>
#include <string.h>
#include <unistd.h>
#include <malloc.h>
#include <stdlib.h>
#include <signal.h>

void *all_signal_nahdler(void *arg) {
    printf("mythread tid = %ld\n", pthread_self());
    return NULL;
}

void sigint_handler(int signo){
    printf(" Received SIGINT\n");
}

void* thread_func_1(){
    sigset_t mask;
    sigfillset(&mask);  // Создаем маску сигналов, которая включает все сигналы
    pthread_sigmask(SIG_BLOCK, &mask, NULL);
    while (1) {
    }

}

void* thread_func_2(){
    struct sigaction sa;

    sa.sa_handler = sigint_handler;
    sa.sa_flags = 0;
    sigemptyset(&sa.sa_mask);   // sigemptyset(&sa.sa_mask); инициализирует маску сигналов sa.sa_mask пустым множеством,
                                // что означает, что во время выполнения обработчика сигнала никакие другие сигналы
                                // не будут заблокированы.

    int err = sigaction(SIGINT, &sa, NULL);
    if (err == -1) {
        printf("Bad sigaction");
        pthread_exit(NULL);
    }

    printf("Press Ctrl+C to send SIGINT.\n");

    while (1) {
    }
}

void* thread_func_3(){
    sigset_t mask;
    int signo;
    sigemptyset(&mask);
    sigaddset(&mask, SIGQUIT);
    pthread_sigmask(SIG_SETMASK, &mask, NULL);
    printf("Wait SIGQUIT\n");
    int err = sigwait(&mask, &signo);
    if (err){
        printf("Bad sigwait()");
        return NULL;
    }
    printf("Signo == %d",signo);

    if (signo == SIGQUIT){
        printf("Received SIGQUIT\n");
    }
}

// struct sigaction - это структура, используемая для определения настроек и обработчика сигнала (signal handler)
// при использовании функции sigaction. Она содержит информацию, необходимую для управления обработкой сигнала.
//
// Основные поля структуры struct sigaction:
//
// void (*sa_handler)(int): Это указатель на функцию, которая будет вызвана при получении сигнала.
// Функция должна иметь следующую сигнатуру: void handler(int signo), где signo - код сигнала.
// Обработчик сигнала может быть установлен в одно из следующих значений:
//      SIG_DFL (по умолчанию): Системный обработчик сигнала.
//      SIG_IGN (игнорирование сигнала): Сигнал будет проигнорирован.
//
// sigset_t sa_mask: Это маска сигналов, которые будут заблокированы при выполнении обработчика сигнала.
// Это предотвращает рекурсивное вызывание сигнала, если он повторно генерируется внутри обработчика.
//
// int sa_flags: Это флаги, управляющие поведением обработчика и другие настройки. Например,
// флаг SA_RESTART может быть установлен, чтобы указать, что системные вызовы, которые были прерваны сигналом,
// должны быть автоматически повторены после выполнения обработчика.


int main() {
    pthread_t tid_1, tid_2, tid_3;
    int err;

    printf("main [%d %d %d]: Hello from main!\n", getpid(), getppid(), gettid());;

    err = pthread_create(&tid_1, NULL, thread_func_1, NULL);
    if (err) {
        printf("main: pthread_create() failed: %s\n", strerror(err));
        return -1;
    }

    err = pthread_create(&tid_2, NULL, thread_func_2, NULL);
    if (err) {
        printf("main: pthread_create() failed: %s\n", strerror(err));
        return -1;
    }

    err = pthread_create(&tid_3, NULL, thread_func_3, NULL);
    if (err) {
        printf("main: pthread_create() failed: %s\n", strerror(err));
        return -1;
    }

    printf("tid1 = %ld, tid2 = %ld, tid3 = %ld\n", tid_1, tid_2, tid_3);
    pthread_join(tid_1, NULL);
    pthread_join(tid_2, NULL);
    pthread_join(tid_3, NULL);

    return 0;
}


