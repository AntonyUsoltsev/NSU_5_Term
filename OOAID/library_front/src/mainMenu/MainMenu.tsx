import React, {CSSProperties} from 'react';
import {Button} from 'antd';
// @ts-ignore
import logoImage from './mainMenu.jpg';
import {useHistory} from "react-router-dom";

const MainMenu = () => {

    const history = useHistory();
    const buttonStyle: CSSProperties = {
        position: 'fixed',
        top: 10,
        left: 10,
        zIndex: 1000,
    };

    const handleButtonClick = () => {
        const booksRoute = "/";
        history.push(booksRoute);
        window.location.reload();
    };

    return (
        <Button style={buttonStyle} onClick={handleButtonClick}>
            <img src={logoImage} alt="Logo" style={{width: '40px', height: '40px'}}/>
        </Button>
    );
};

export default MainMenu;
