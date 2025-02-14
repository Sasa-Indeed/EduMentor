import React from 'react';
import { Card, Button } from 'react-bootstrap';

const MentorCard = ({ name, bio, image }) => {
  return (
    <Card className="mb-4 shadow-sm home-mentor-card">
      <Card.Img variant="top" src={image}/>
      <Card.Body>
        <Card.Title>{name}</Card.Title>
        <Card.Text>{bio}</Card.Text>
        <Button className='home-button'>Learn More</Button>
      </Card.Body>
    </Card>
  );
};

export default MentorCard;
