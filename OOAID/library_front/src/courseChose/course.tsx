import React, {useState} from 'react';
import {Button} from 'antd';
import SubjectList from "../subjectChose/subject";
import "./course.css"

const CourseList = ({university, onCourseSelect}) => {
    const courseData = {
        НГУ: ['1 курс', '2 курс', '3 курс', '4 курс'],
        ТГУ: ['1 курс', '2 курс', '3 курс', '4 курс'],
        НГТУ: ['1 курс', '2 курс', '3 курс'],
    };

    const [selectedCourse, setSelectedCourse] = useState(null);
    const [showSubjectList, setShowSubjectList] = useState(false);

    const handleCourseClick = (course: any) => {
        setSelectedCourse(course);
        setShowSubjectList(true);
    };

    const handleSubjectSelect = (subject: any) => {
        // Обработка выбранного предмета, например, отправка данных на сервер или переход к следующему этапу
        console.log('Selected Subject:', subject);
        // Добавьте здесь вашу логику

        // Скрыть SubjectList после выбора предмета
        //setShowSubjectList(false);
    };

    return (
        <div>
            <header className="course-header">Выберите курс для {university.name}</header>
            <div className="course-buttons-container">
                {courseData[university.name].map((course, index) => (
                    <Button
                        key={index}
                        type={selectedCourse === course ? 'primary' : 'default'}
                        onClick={() => handleCourseClick(course)}
                        className="course-button">
                        {course}
                    </Button>
                ))}
            </div>
            {showSubjectList && (
                <SubjectList
                    course={selectedCourse}
                    onSubjectSelect={handleSubjectSelect}
                    // onNextClick={() => setShowSubjectList(false)}
                    onNextClick={() => console.log("Next clicked")}
                />
            )}
        </div>
    );
};

export default CourseList;