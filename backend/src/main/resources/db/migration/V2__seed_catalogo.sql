INSERT INTO categoria (nome) VALUES ('Clássicos'), ('Chocolate'), ('Frutas'), ('Especiais');

INSERT INTO produto (categoria_id, nome, descricao, estoque, preco, foto_url) VALUES
-- Clássicos
((SELECT id FROM categoria WHERE nome = 'Clássicos'), 'Baunilha Clássico',
 'Massa de baunilha com buttercream de baunilha e confeitos coloridos', 40, 9.90, '/img/produtos/baunilha.jpg'),
((SELECT id FROM categoria WHERE nome = 'Clássicos'), 'Cenoura com Chocolate',
 'Massa de cenoura com cobertura cremosa de chocolate ao leite', 35, 10.90, '/img/produtos/cenoura.jpg'),
((SELECT id FROM categoria WHERE nome = 'Clássicos'), 'Doce de Leite',
 'Massa amanteigada recheada com doce de leite e cobertura de chantilly', 25, 11.50, '/img/produtos/doce-de-leite.jpg'),

-- Chocolate
((SELECT id FROM categoria WHERE nome = 'Chocolate'), 'Brigadeiro Belga',
 'Massa de chocolate 70% com recheio de brigadeiro e granulado belga', 30, 12.90, '/img/produtos/brigadeiro.jpg'),
((SELECT id FROM categoria WHERE nome = 'Chocolate'), 'Ninho com Nutella',
 'Massa branca com recheio de Nutella e cobertura de leite Ninho', 30, 13.90, '/img/produtos/ninho-nutella.jpg'),
((SELECT id FROM categoria WHERE nome = 'Chocolate'), 'Chocolate Branco com Pistache',
 'Massa de chocolate branco com ganache de pistache', 15, 15.90, '/img/produtos/pistache.jpg'),

-- Frutas
((SELECT id FROM categoria WHERE nome = 'Frutas'), 'Red Velvet com Morango',
 'Massa red velvet, cobertura de cream cheese e morangos frescos', 0, 14.50, '/img/produtos/red-velvet.jpg'),
((SELECT id FROM categoria WHERE nome = 'Frutas'), 'Limão Siciliano',
 'Massa de limão siciliano com recheio de curd e merengue maçaricado', 20, 12.50, '/img/produtos/limao.jpg'),
((SELECT id FROM categoria WHERE nome = 'Frutas'), 'Frutas Vermelhas',
 'Massa de baunilha com geleia de frutas vermelhas e chantilly', 20, 13.50, '/img/produtos/frutas-vermelhas.jpg'),

-- Especiais
((SELECT id FROM categoria WHERE nome = 'Especiais'), 'Paçoca',
 'Massa de amendoim com recheio cremoso de paçoca', 25, 11.90, '/img/produtos/pacoca.jpg'),
((SELECT id FROM categoria WHERE nome = 'Especiais'), 'Churros',
 'Massa com canela e açúcar, recheio de doce de leite', 25, 12.90, '/img/produtos/churros.jpg'),
((SELECT id FROM categoria WHERE nome = 'Especiais'), 'Café com Avelã',
 'Massa de café expresso com cobertura de creme de avelã', 10, 14.90, '/img/produtos/cafe-avela.jpg');


