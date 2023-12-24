import React from 'react';
import { Button } from 'antd';
import { useHistory } from "react-router-dom";
// @ts-ignore
import logoImage from './mainMenu.jpg';

const MainMenu = () => {
    const history = useHistory();

    const handleButtonClick = () => {
        const booksRoute = "/";
        history.push(booksRoute);
        window.location.reload();
    };

    return (
        <Button
            style={{ padding: 0, border: 'none' }}
            onClick={handleButtonClick}
        >
            <img src={logoImage} alt="Logo" style={{ width: '80px', height: '60px' }} />
        </Button>
    );
};

export default MainMenu;
