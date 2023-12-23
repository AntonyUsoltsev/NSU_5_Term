// BookListPage.jsx
import React, {useEffect, useState} from 'react';
import {Button, List} from 'antd';
import PostService from '../postService/PostService';
import {useHistory, useParams} from 'react-router-dom';

const BookListPage = () => {
    const [books, setBooks] = useState([]);
    const history = useHistory();
    const {university, course, subject} = useParams();
    useEffect(() => {
        PostService.getBooks(university, course, subject).then((response: any) => {
            setBooks(response.data);
        });
    }, [university, course, subject]);

    const handleBackClick = () => {
        history.push('/');
    };

    return (
        <div>
            <header className="book-list-header">Список книг для {subject} предмета</header>
            <List
                dataSource={books}
                renderItem={(item) => (
                    <List.Item>
                        {item.title} - {item.author}
                    </List.Item>
                )}
            />
            <Button type="primary" onClick={handleBackClick}>Назад</Button>
        </div>
    );
};

export default BookListPage;
