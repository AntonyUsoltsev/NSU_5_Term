// SubjectList.jsx
import React, { useEffect, useState } from 'react';
import { Select, Button, Spin } from 'antd';
import './subject.css';
import PostService from '../postService/PostService';

const SubjectList = ({ university, course }) => {
    const [loading, setLoading] = useState(true);
    const [subjects, setSubjects] = useState([]);
    const [selectedSubject, setSelectedSubject] = useState(null);

    useEffect(() => {
        // Загрузка списка предметов для выбранного курса
        PostService.getSubjects(university.name, course).then((response) => {
            setSubjects(response.data);
            setLoading(false);
        });
    }, [university, course]);

    const handleSubjectChange = (value) => {
        setSelectedSubject(value);
    };

    const handleContinueClick = () => {
        if (selectedSubject) {
            // Обработка выбранного предмета, например, отправка данных на сервер или переход к следующему этапу
            console.log('Selected Subject:', selectedSubject);
        }
    };

    return (
        <div className="subject-container">
            <header className="subject-header">Выберите предмет для {course}</header>
            {loading ? (
                <Spin size="large" />
            ) : (
                <>
                    <Select
                        className="subject-select"
                        placeholder="Выберите предмет"
                        onChange={handleSubjectChange}
                        value={selectedSubject}>
                        {subjects.map((subject, index) => (
                            <Select.Option key={index} value={subject}>
                                {subject.name}
                            </Select.Option>
                        ))}
                    </Select>
                    <div className="subject-buttons">
                        <Button
                            type="primary"
                            onClick={handleContinueClick}
                            disabled={!selectedSubject}>
                            Продолжить
                        </Button>
                    </div>
                </>
            )}
        </div>
    );
};

export default SubjectList;
