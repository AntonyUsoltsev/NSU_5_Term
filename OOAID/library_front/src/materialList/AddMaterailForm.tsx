// AddMaterialForm.jsx
import React from 'react';
import { Form, Input, Button } from 'antd';

const AddMaterialForm = ({ onFinish }) => {
  const [form] = Form.useForm();

  return (
    <Form
      name="addMaterial"
      onFinish={onFinish}
      form={form}
    >
      <Form.Item
        name="author"
        rules={[{ required: true, message: 'Пожалуйста, введите автора' }]}
      >
       < Input placeholder="Введите автора"/>
      </Form.Item>
      <Form.Item
        label="Название"
        name="name"
        rules={[{ required: true, message: 'Пожалуйста, введите название' }]}
      >
        < Input placeholder="Введите название"/>
      </Form.Item>
      <Form.Item
        label="Ссылка на книгу"
        name="link"
        rules={[{ required: true, message: 'Пожалуйста, введите ссылку на книгу' }]}
      >
        < Input placeholder="Введите ссылку для просмотра материала"/>
      </Form.Item>
      <Form.Item>
        <Button type="primary" htmlType="submit">
          Добавить материал
        </Button>
      </Form.Item>
    </Form>
  );
};

export default AddMaterialForm;
