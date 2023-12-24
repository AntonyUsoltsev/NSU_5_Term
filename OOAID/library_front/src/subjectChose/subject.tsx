// SubjectList.jsx
import React, {useEffect, useState} from 'react';
import {Select, Button} from 'antd';
import {useHistory} from 'react-router-dom';
import './subject.css';
import PostService from '../postService/PostService';

const SubjectList = ({university, course}: any) => {
    const [subjects, setSubjects] = useState([]);
    const [selectedSubject, setSelectedSubject] = useState(null);
    const history = useHistory();

    useEffect(() => {
        // Загрузка списка предметов для выбранного курса
        PostService.getSubjects(university.name, course).then((response: any) => {
            setSubjects(response.data);
        });
    }, [university, course]);

    const handleSubjectChange = (value: any, option: any) => {
        console.log(value)
        console.log(option)
        // Сохранение объекта предмета целиком
        setSelectedSubject(option);
    };

    const handleContinueClick = () => {
        if (selectedSubject) {
            // Перенаправление на страницу с книгами, передавая параметры университета, курса и предмета
            const booksRoute = `/student_compass/${university.name}/${course}/${selectedSubject.data_id}`;
            history.push(booksRoute);
        }
    };

    return (
        <div className="subject-container">
            <header className="subject-header">Выберите предмет для {course} курса</header>
            <Select
                className="subject-select"
                placeholder="Выберите предмет"
                onChange={handleSubjectChange}
                value={selectedSubject?.name} // Вывод названия выбранного предмета
            >
                {subjects.map((subject: any, index) => (
                    <Select.Option key={index} value={subject.name}  data_id={subject.id}>
                        {subject.name}
                    </Select.Option>
                ))}
            </Select>
            <div className="subject-buttons">
                <Button type="primary" onClick={handleContinueClick} disabled={!selectedSubject}>
                    Продолжить
                </Button>
            </div>
        </div>
    );
};

export default SubjectList;
