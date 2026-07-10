-- Preguntas típicas al estilo "100 Mexicanos Dijeron"
INSERT INTO questions (text) VALUES
('¿Qué harías si te ganaras la lotería?'),
('¿Cuál es la comida mexicana más popular?'),
('¿Qué llevas a una fiesta de cumpleaños?'),
('¿Cuál es el mejor programa de la televisión mexicana?'),
('¿Qué haces en Navidad?'),
('¿Cuál es la palabra que más dices al día?'),
('¿Qué estudiarías si pudieras empezar de nuevo?'),
('¿Cuál es el mejor destino para viajar en México?'),
('¿Qué no puede faltar en un altar de muertos?'),
('¿Cuál es la mejor excusa para llegar tarde al trabajo?');

-- Respuestas para pregunta 1: ¿Qué harías si te ganaras la lotería?
INSERT INTO answers (question_id, text, score) VALUES
(1, 'Comprar una casa', 35),
(1, 'Viajar por el mundo', 25),
(1, 'Ayudar a mi familia', 20),
(1, 'Invertir o ahorrar', 10),
(1, 'Comprar un coche', 5),
(1, 'Pagar deudas', 5);

-- Respuestas para pregunta 2: ¿Cuál es la comida mexicana más popular?
INSERT INTO answers (question_id, text, score) VALUES
(2, 'Tacos', 40),
(2, 'Mole', 15),
(2, 'Pozole', 15),
(2, 'Tamales', 10),
(2, 'Chiles en nogada', 10),
(2, 'Guacamole', 10);

-- Respuestas para pregunta 3: ¿Qué llevas a una fiesta de cumpleaños?
INSERT INTO answers (question_id, text, score) VALUES
(3, 'Pastel', 40),
(3, 'Regalo', 30),
(3, 'Refresco', 10),
(3, 'Globos', 10),
(3, 'Botana', 5),
(3, 'Música', 5);

-- Respuestas para pregunta 4: ¿Cuál es el mejor programa de la televisión mexicana?
INSERT INTO answers (question_id, text, score) VALUES
(4, 'El Chavo del 8', 35),
(4, '100 Mexicanos Dijeron', 25),
(4, 'La Rosa de Guadalupe', 15),
(4, 'Vecinos', 10),
(4, 'El Chapulín Colorado', 10),
(4, 'MasterChef México', 5);

-- Respuestas para pregunta 5: ¿Qué haces en Navidad?
INSERT INTO answers (question_id, text, score) VALUES
(5, 'Cenar con la familia', 40),
(5, 'Abrir regalos', 20),
(5, 'Ir a misa', 15),
(5, 'Hacer posada', 10),
(5, 'Poner el árbol', 10),
(5, 'Dar las gracias', 5);

-- Respuestas para pregunta 6: ¿Cuál es la palabra que más dices al día?
INSERT INTO answers (question_id, text, score) VALUES
(6, 'Gracias', 25),
(6, 'Hola', 20),
(6, '¿Qué onda?', 15),
(6, 'No mames', 15),
(6, 'OK', 10),
(6, 'Güey o wey', 15);

-- Respuestas para pregunta 7: ¿Qué estudiarías si pudieras empezar de nuevo?
INSERT INTO answers (question_id, text, score) VALUES
(7, 'Medicina', 25),
(7, 'Ingeniería', 20),
(7, 'Programación o sistemas', 15),
(7, 'Arte o diseño', 10),
(7, 'Negocios o administración', 15),
(7, 'Derecho', 15);

-- Respuestas para pregunta 8: ¿Cuál es el mejor destino para viajar en México?
INSERT INTO answers (question_id, text, score) VALUES
(8, 'Cancún', 30),
(8, 'CDMX', 20),
(8, 'Puerto Vallarta', 15),
(8, 'Los Cabos', 10),
(8, 'Guadalajara', 10),
(8, 'Oaxaca', 10),
(8, 'Mérida', 5);

-- Respuestas para pregunta 9: ¿Qué no puede faltar en un altar de muertos?
INSERT INTO answers (question_id, text, score) VALUES
(9, 'Cempasúchil o flor de muerto', 30),
(9, 'Fotos de los difuntos', 25),
(9, 'Pan de muerto', 20),
(9, 'Velas o veladoras', 10),
(9, 'Calaveras de azúcar', 10),
(9, 'Agua', 5);

-- Respuestas para pregunta 10: ¿Cuál es la mejor excusa para llegar tarde al trabajo?
INSERT INTO answers (question_id, text, score) VALUES
(10, 'Tráfico', 40),
(10, 'El despertador no sonó', 20),
(10, 'Problemas en el transporte', 15),
(10, 'Se descompuso el coche', 10),
(10, 'Enfermedad', 10),
(10, 'Se me olvidó algo', 5);
