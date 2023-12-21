
import React from 'react';
import {BrowserRouter as Router} from 'react-router-dom';
import UniversityPage from './universityChose/university';
import './App.css';


const App = () => {
    return (
        <div className="App">
            <header className="App-header">
                <UniversityPage/>

                {/*<p>*/}
                {/*    Edit <code>src/App.jsx</code> and save to reload.*/}
                {/*</p>*/}

                {/*<a*/}
                {/*    className="App-link"*/}
                {/*    href="https://reactjs.org"*/}
                {/*    target="_blank"*/}
                {/*    rel="noopener noreferrer"*/}
                {/*>*/}
                {/*    Learn React*/}
                {/*</a>*/}
            </header>
        </div>
    );
}

const Root = () => {
    return (
        <Router>
            <App/>
        </Router>
    );
}

export default Root;