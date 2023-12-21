// UniversityPage.jsx

import React, {useState} from 'react';
import {Row, Col} from 'antd';
import {Link} from 'react-router-dom';
import CourseList from '../courseChose/course'; // Предполагается, что у вас есть компонент CourseList
import './university.css';

// @ts-ignore
import nsuImg from './nsuLogo.jpg';
// @ts-ignore
import tsuImg from './tsuLogo.png';
// @ts-ignore
import nstuImg from './nstuLogo.png';

const UniversityPage: React.FC = () => {
    const universities = [
        {name: 'НГУ', link: '/nsu', icon: nsuImg},
        {name: 'ТГУ', link: '/tsu', icon: tsuImg},
        {name: 'НГТУ', link: '/nstu', icon: nstuImg},
    ];

    const [selectedUniversity, setSelectedUniversity] = useState(null);

    const handleUniversityClick = (university) => {
        setSelectedUniversity(university);
    };

    return (
        <div>
            <header className="university-header">Выберите университет</header>
            <Row gutter={[48, 16]} justify="center" align="middle">
                {universities.map((university, index) => (
                    <Col key={index} xs={24} sm={12} md={8} lg={4}>
                        <div onClick={() => handleUniversityClick(university)}>
                            <Link to={university.link}>

                                <img
                                    src={university.icon}
                                    alt={university.name}
                                    className="icon"
                                />

                            </Link>
                        </div>
                    </Col>
                ))}
            </Row>
            {selectedUniversity && <CourseList university={selectedUniversity}
                                               onCourseSelect={(course) => console.log('Selected course:', course)}/>}
        </div>

    );
};

export default UniversityPage;
