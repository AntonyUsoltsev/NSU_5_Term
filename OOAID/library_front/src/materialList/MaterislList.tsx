// BookList.jsx
import React, { useState, useEffect } from 'react';
import { List, Button, message } from 'antd';
import PostService from "../postService/PostService";
import ReviewList from "../reviewList/ReviewList";
import { useParams } from "react-router-dom";
import "./MaterialStyle.css"

const BookList = () => {
    const [data, setData]: any = useState();
    const [books, setBooks]: any = useState([]);
    const [reviews, setReviews]: any = useState([]);

    const { university, course, subject }: any = useParams();

    useEffect(() => {
        // Загрузка списка книг для выбранного предмета
        PostService.getBooks(university, course, subject).then((response: any) => {
            const inputData = response.data;
            setData(inputData);
            setBooks(inputData.materials)
            setReviews(inputData.reviews)
        });
    }, [university, course, subject]);

    const handleDownload = (book: any) => {
        const isUserAuthenticated = checkUserAuthentication();

        if (isUserAuthenticated) {
            // Выполнить скачивание книги
            downloadBook(book);
        } else {
            // Показать предупреждение
            showWarning();
        }
    };

    const downloadBook = (book) => {
        // Здесь реализуйте логику скачивания книги
        console.log(`Загрузка книги: ${book.title}`);
    };

    const checkUserAuthentication = () => {
        // Временная заглушка (замените на реальную логику)
        const isAuthenticated = localStorage.getItem('token') !== null;
        return isAuthenticated;
    };

    const showWarning = () => {
        message.warning('Прежде чем скачать, необходимо авторизоваться.');
    };

    return (
        <div>
            <header className="subjects-header">Список книг для предмета {data.name}</header>
            <List
                dataSource={books}
                renderItem={(item: any) => (
                    <List.Item>
                        {item.title} - {item.author}
                        <Button onClick={() => handleDownload(item)}>Скачать</Button>
                    </List.Item>
                )}
            />
            <ReviewList selectedSubject={data.name} inputReviews={reviews} />
        </div>
    );
};

export default BookList;
