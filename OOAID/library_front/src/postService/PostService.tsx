import axios from "axios";

export default class PostService {
    static errorHandler(error: any) {
        if (error.response) {
            console.error(error.response.data);
            console.error(error.response.status);
            console.error(error.response.headers);
        } else if (error.request) {
            console.error(error.request);
        } else {
            console.error('Error', error.message);
        }
    }

    static async getUniversities(tableId: any) {
        console.log("tableId" + tableId)
        try {
            const value = await axios.get("http://localhost:8080/student_compass");
            console.log(value)
            return value;
        } catch (error) {
            this.errorHandler(error);
        }
    }

    static async getCourses(universityName: any) {
        try {
            const value = await axios.get("http://localhost:8080/student_compass/" + universityName);
            console.log(value)
            return value;
        } catch (error) {
            this.errorHandler(error);
        }
    }

    static async getSubjects(universityName: string, courseValue: string) {
        try {
            const value = await axios.get("http://localhost:8080/student_compass/" +
                universityName + "/" +
                courseValue);
            console.log(value)
            return value;
        } catch (error) {
            this.errorHandler(error);
        }
    }

    static async getBooks(universityName: string, courseValue: string, selectedSubject: string) {
        try {
            const value = await axios.get("http://localhost:8080/student_compass/" +
                universityName + "/" +
                courseValue + "/" +
                selectedSubject);
            console.log(value)
            return value;
        } catch (error) {
            this.errorHandler(error);
        }
    }

}