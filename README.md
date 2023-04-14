# Blockchain
Репозиторий содержит простую реализацию блокчейна

# Инструкция по запуску
## 1. Запуск JAR файла
### 1.1. Клонировать репозиторий
```
git clone https://github.com/MaksimZotov/blockchain.git
```
### 1.2. Перейти в папку проекта
```
cd blockchain
```
### 1.3. Запустить сборку
```
./gradlew jar
```
### 1.4. Перейти в директорию со сгенерированным JAR файлом
```
cd build/libs
```
### 1.5. Запустить JAR файл
#### 1.5.1. Команда для запуска
```
java -jar blockchain.jar -g -a 0.0.0.0:8080 -c i -n http://0.0.0.0:8081 -n http://0.0.0.0:8082
```
#### 1.5.2. Аргументы
* `-g` - генерирует ли нода `genesis-блок` (необязательный аргумент)
* `-a` - IP-адрес и порт ноды
* `-с` - алгоритм подбора `nonce`
  * `i` - инкремент
  * `d` - декремент
  * `r` - рандом
* `-n` - соседние ноды в виде `http://0.0.0.0:8080`

## 2. Запуск в Docker окружении
### 2.1. Клонировать репозиторий
```
git clone https://github.com/MaksimZotov/blockchain.git
```
### 2.2. Перейти в папку проекта
```
cd blockchain
```
### 2.3. Создать образ
```
docker build -t blockchain .
```
### 2.4. Запустить docker-compose
```
docker-compose up
```

# Тестирование
## 1. Результаты
### 1.1. Ветка `main`
[![Tests](https://github.com/MaksimZotov/blockchain/actions/workflows/tests.yml/badge.svg?branch=main)](https://github.com/MaksimZotov/text-occurrences-finder/actions/workflows/tests.yml)
### 1.2. Ветка `develop`
[![Tests](https://github.com/MaksimZotov/blockchain/actions/workflows/tests.yml/badge.svg?branch=develop)](https://github.com/MaksimZotov/text-occurrences-finder/actions/workflows/tests.yml)
## 2. Описание тестов
### 2.1. Модульные тесты
* `testCorrectGenesisBlock`
* `testIncorrectGenesisBlock`
* `testCorrectSecondBlock`
* `testIncorrectSecondBlock`
* `testCorrectBlocksList`
* `testIncorrectBlocksList`
### 2.2. Интеграционные тесты
* `testNotificationsAboutAddedBlocks`
* `testBlocksConsistency`
* `testMinority`
* `testMinorityWithIncorrectBlocksFromNeighbour`

# Примеры работы
## 1. Первая нода
![](/.images/node1.png)
## 2. Вторая нода
![](/.images/node2.png)
## 3. Третья нода
![](/.images/node3.png)
