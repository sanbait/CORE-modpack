// Priority: 100

// Логируем начало загрузки серверных скриптов
console.info('CORE: Loading Server Scripts...');

ServerEvents.loaded(event => {
    console.info('CORE: Server Loaded Successfully!');
});
