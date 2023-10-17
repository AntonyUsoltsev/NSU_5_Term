#define _GNU_SOURCE

#include <stdio.h>
#include <pthread.h>
#include <string.h>
#include <sys/types.h>
#include <unistd.h>
#include <signal.h>

void *thread_func_1() {
    sigset_t mask;
    sigfillset(&mask);  // Создаем маску сигналов, которая включает все сигналы
    pthread_sigmask(SIG_BLOCK, &mask, NULL);
    for (int i = 0; i < 3; i++) {
        pthread_kill(pthread_self(), SIGSEGV);
        printf("Thread 1 blocked all signals\n");
    }
    return NULL;
}

void sigint_handler(int signo) {
    printf("Received SIGINT\n");
}

void *thread_func_2() {
    signal(SIGINT, sigint_handler);

    printf("Press Ctrl+C to send SIGINT.\n");


//    while (1) {
//
//    }
}

void *thread_func_3() {
    sigset_t mask;
    int signo;
    sigemptyset(&mask);
    sigaddset(&mask, SIGQUIT);
    printf("Wait SIGQUIT\n");
    while (1) {
        int err = sigwait(&mask, &signo);
        if (err) {
            printf("Bad sigwait()");
            return NULL;
        }
        //printf("Signo == %d\n", signo);

        if (signo == SIGQUIT) {
            printf("Received SIGQUIT\n");
        }
    }
    return NULL;
}

int main() {
    pthread_t tid_1, tid_2, tid_3;
    int err;

    printf("main [%d %d %d]: Hello from main!\n", getpid(), getppid(), gettid());;

    err = pthread_create(&tid_1, NULL, thread_func_1, NULL);
    if (err) {
        printf("main: pthread_create_1() failed: %s\n", strerror(err));
        return -1;
    }

    err = pthread_create(&tid_2, NULL, thread_func_2, NULL);
    if (err) {
        printf("main: pthread_create_2() failed: %s\n", strerror(err));
        return -1;
    }

    err = pthread_create(&tid_3, NULL, thread_func_3, NULL);
    if (err) {
        printf("main: pthread_create_3() failed: %s\n", strerror(err));
        return -1;
    }

    pthread_join(tid_1, NULL);
    pthread_join(tid_2, NULL);
    sleep(1);
    pthread_kill(tid_3, SIGQUIT);
    sleep(1);
    pthread_kill(tid_3, SIGQUIT);
    sleep(1);
    pthread_kill(tid_3, SIGQUIT);
    pthread_join(tid_3, NULL);
    return 0;
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





