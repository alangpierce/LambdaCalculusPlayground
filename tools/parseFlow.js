const exec = require('child_process').exec;

exec('./node_modules/.bin/flow --json', (error, stdout, stderr) => {
    if (stderr) {
        console.log('Flow process gave error:');
        console.log(stderr);
        process.exit(1);
    }
    const results = JSON.parse(stdout);
    results.errors.forEach((error) => {
        const messageStr = error.message.map((m) => m.descr).join('  ');
        const message = error.message[0];
        console.log(
            `${message.path}:${message.line}:${message.start} ${messageStr}`);
    });
    if (!results.passed) {
        process.exit(1);
    }
});
