// BookList.jsx
import React, {useState, useEffect} from 'react';
import {List, Button, Modal} from 'antd';
import AuthenticationForm from '../authorizitaion/AuthPage';
import PostService from "../postService/PostService";
import ReviewList from "../reviewList/ReviewList"; // Импортируйте компонент AuthenticationForm

const BookList = ({university, course, subject}) => {
    const [books, setBooks] = useState([]);

    useEffect(() => {
        // Загрузка списка книг для выбранного предмета
        PostService.getBooks(university.name, course.number, subject.id).then((response: any) => {
            const sortedCourses = response.data;
            setBooks(sortedCourses);
        });
    }, [university, course, subject]);

    const handleDownload = (book: any) => {
        // Проверка авторизации пользователя перед скачиванием книги
        // Если пользователь авторизован, выполнить скачивание
        // Иначе, отобразить форму авторизации/регистрации
        const isUserAuthenticated = checkUserAuthentication(); // Реализуйте функцию checkUserAuthentication

        if (isUserAuthenticated) {
            // Выполнить скачивание книги
            downloadBook(book);
        } else {
            // Отобразить форму авторизации/регистрации
            showAuthenticationForm();
        }
    };

    const downloadBook = (book) => {
        // Здесь реализуйте логику скачивания книги
        console.log(`Загрузка книги: ${book.title}`);
    };

    const checkUserAuthentication = () => {
        // Реализуйте логику проверки авторизации пользователя на бэкенде
        // Верните true, если пользователь авторизован, и false в противном случае
        // Пример: вам может потребоваться хранение токена пользователя в cookies или localStorage
        // и проверка его валидности на бэкенде

        // Временная заглушка (замените на реальную логику)
        const isAuthenticated = localStorage.getItem('token') !== null;
        return isAuthenticated;
    };

    const showAuthenticationForm = () => {
        // Отобразить модальное окно с формой авторизации/регистрации
        Modal.info({
            title: 'Внимание',
            content: (
                <div>
                    <p>Для скачивания книги необходимо войти или зарегистрироваться.</p>
                    {/* Интегрируйте компонент с формой авторизации/регистрации */}
                    <AuthenticationForm
                        onAuthenticationSuccess={handleAuthenticationSuccess}
                        onRegistrationSuccess={handleRegistrationSuccess}
                    />
                </div>
            ),
            onOk() {
                // Обработчик закрытия модального окна
                console.log('Модальное окно закрыто');
            },
        });
    };

    const handleAuthenticationSuccess = () => {
        // Обработчик успешной авторизации
        // Вы можете выполнить необходимые действия, например, обновить компонент или выполнить скачивание книги
        console.log('Успешная авторизация');
        // Продолжить скачивание книги или другие действия
        // ...

        // Закрыть модальное окно
        Modal.destroyAll();
    };

    const handleRegistrationSuccess = () => {
        // Обработчик успешной регистрации
        // Вы можете выполнить необходимые действия, например, обновить компонент или выполнить скачивание книги
        console.log('Успешная регистрация');
        // Продолжить скачивание книги или другие действия
        // ...

        // Закрыть модальное окно
        Modal.destroyAll();
    };

    return (
        <div>
            <header className="book-list-header">Список книг для {subject} предмета</header>
            <List
                dataSource={books}
                renderItem={(item) => (
                    <List.Item>
                        {item.title} - {item.author}
                        <Button onClick={() => handleDownload(item)}>Скачать</Button>
                    </List.Item>
                )}
            />
            <ReviewList university={university} course={course} selectedSubject={subject} />
        </div>
    );
};

export default BookList;
