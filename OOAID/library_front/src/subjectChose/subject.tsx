// SubjectList.jsx

import React, {useState, useEffect} from 'react';
import {Select, Button} from 'antd';
import './subject.css';

const SubjectList = ({course, onSubjectSelect, onNextClick}) => {
    const [subjects, setSubjects] = useState([]);
    const [selectedSubject, setSelectedSubject] = useState(null);

    // Пример загрузки предметов для выбранного курса (замените на вашу логику загрузки)
    useEffect(() => {
        // Загружаем список предметов для выбранного курса
        // В данном примере, используем фиктивные данные
        const mockSubjects = [
            'Сетевые технологии',
            'Операционные системы',
            'Вычмат',
            'Физра',
            'Элтех',
            'МТК',
            'ООАиД',
        ];

        setSubjects(mockSubjects);
    }, [course]);

    const handleSubjectChange = (value) => {
        setSelectedSubject(value);
    };

    const handleContinueClick = () => {
        if (selectedSubject) {
            // Передаем выбранный предмет обратно в родительский компонент
            onSubjectSelect(selectedSubject);
        }
    };

    return (
        <div className="subject-container">
            <header className="subject-header">Выберите предмет для {course}</header>
            <Select
                className="subject-select"
                placeholder="Выберите предмет"
                onChange={handleSubjectChange}
                value={selectedSubject}>
                {subjects.map((subject, index) => (
                    <Select.Option key={index} value={subject}>
                        {subject}
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
        </div>
    );
};

export default SubjectList;
