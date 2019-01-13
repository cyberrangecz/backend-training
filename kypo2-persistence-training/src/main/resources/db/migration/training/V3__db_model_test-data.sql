INSERT INTO pre_hook(id) VALUES (nextval('pre_hook_id_seq'));
INSERT INTO pre_hook(id) VALUES (nextval('pre_hook_id_seq'));
INSERT INTO pre_hook(id) VALUES (nextval('pre_hook_id_seq'));
INSERT INTO pre_hook(id) VALUES (nextval('pre_hook_id_seq'));
INSERT INTO pre_hook(id) VALUES (nextval('pre_hook_id_seq'));
INSERT INTO pre_hook(id) VALUES (nextval('pre_hook_id_seq'));

INSERT INTO post_hook(id) VALUES (nextval('post_hook_id_seq'));
INSERT INTO post_hook(id) VALUES (nextval('post_hook_id_seq'));
INSERT INTO post_hook(id) VALUES (nextval('post_hook_id_seq'));
INSERT INTO post_hook(id) VALUES (nextval('post_hook_id_seq'));
INSERT INTO post_hook(id) VALUES (nextval('post_hook_id_seq'));
INSERT INTO post_hook(id) VALUES (nextval('post_hook_id_seq'));

INSERT INTO abstract_level(id, max_score, next_level, title, post_hook_id, pre_hook_id) VALUES (nextval('abstract_level_id_seq'), 20, 7, 'Game Level1', 1, 1);
INSERT INTO abstract_level(id, max_score, next_level, title, post_hook_id, pre_hook_id) VALUES (nextval('abstract_level_id_seq'), 55, 8, 'Game Level2', 4, 4);
INSERT INTO abstract_level(id, max_score, next_level, title, post_hook_id, pre_hook_id) VALUES (nextval('abstract_level_id_seq'), 70, 9, 'Game Level Test', 2, 2);
INSERT INTO abstract_level(id, max_score, next_level, title, post_hook_id, pre_hook_id) VALUES (nextval('abstract_level_id_seq'), 70, 1, 'Info Level1', 2, 2);
INSERT INTO abstract_level(id, max_score, next_level, title, post_hook_id, pre_hook_id) VALUES (nextval('abstract_level_id_seq'), 13, 2, 'Info Level2', 5, 5);
INSERT INTO abstract_level(id, max_score, next_level, title, post_hook_id, pre_hook_id) VALUES (nextval('abstract_level_id_seq'), 0, 3, 'Info Level Test', 3, 3);
INSERT INTO abstract_level(id, max_score, next_level, title, post_hook_id, pre_hook_id) VALUES (nextval('abstract_level_id_seq'), 50, null, 'Assessment Level1', 3, 3);
INSERT INTO abstract_level(id, max_score, next_level, title, post_hook_id, pre_hook_id) VALUES (nextval('abstract_level_id_seq'), 75, null, 'Assessment Level2', 6, 6);
INSERT INTO abstract_level(id, max_score, next_level, title, post_hook_id, pre_hook_id) VALUES (nextval('abstract_level_id_seq'), 100, null, 'Assessment Level Test', 1, 1);

INSERT INTO abstract_level(id, max_score, next_level, title, post_hook_id, pre_hook_id) VALUES (nextval('abstract_level_id_seq'), 0, 11, 'Network Error - Info', null, null);
INSERT INTO abstract_level(id, max_score, next_level, title, post_hook_id, pre_hook_id) VALUES (nextval('abstract_level_id_seq'), 20, 12, 'Network Error', null, null);
INSERT INTO abstract_level(id, max_score, next_level, title, post_hook_id, pre_hook_id) VALUES (nextval('abstract_level_id_seq'), 0, 13, 'New Secure Connection - Info', null, null);
INSERT INTO abstract_level(id, max_score, next_level, title, post_hook_id, pre_hook_id) VALUES (nextval('abstract_level_id_seq'), 20, 14, 'New Secure Connection', null, null);
INSERT INTO abstract_level(id, max_score, next_level, title, post_hook_id, pre_hook_id) VALUES (nextval('abstract_level_id_seq'), 0, 15, 'Unavailable Document - Info', null, null);
INSERT INTO abstract_level(id, max_score, next_level, title, post_hook_id, pre_hook_id) VALUES (nextval('abstract_level_id_seq'), 35, 16, 'Unavailable Document', null, null);
INSERT INTO abstract_level(id, max_score, next_level, title, post_hook_id, pre_hook_id) VALUES (nextval('abstract_level_id_seq'), 0, 17, 'Disable Passwords - Info', null, null);
INSERT INTO abstract_level(id, max_score, next_level, title, post_hook_id, pre_hook_id) VALUES (nextval('abstract_level_id_seq'), 25, null, 'Disable Passwords', null, null);



INSERT INTO game_level(id, attachments, estimated_duration, incorrect_flag_limit, content, flag, solution, solution_penalized) VALUES (1, null, 25, 5, 'Play me', 'secretFlag', 'This is how you do it', true );
INSERT INTO game_level(id, attachments, estimated_duration, incorrect_flag_limit, content, flag, solution, solution_penalized) VALUES (2, null, 60, 3, 'Unsolvable problem', 'jibberish', 'Not sure yet', false);
INSERT INTO game_level(id, attachments, estimated_duration, incorrect_flag_limit, content, flag, solution, solution_penalized) VALUES (3, null, 25, 5, 'Play me', 'secretFlag', 'correct flag', true);

INSERT INTO game_level(id, attachments, estimated_duration, incorrect_flag_limit, content, flag, solution, solution_penalized) VALUES (11, null, 5, 5, '<style type="text/css">
  .prompt {color: #B7B7B7; font-weight: bold;}
  .command {color:white;}
</style>

<p>
  <b>Just read it!</b>
</p>

<ol style="margin-bottom: 2em">
  <li>Right-click on any TELNET packet and select option <b>"Follow → TCP stream"</b>.</li>
  <li>Read the conversation and search for "FLAG".</li>
</ol>', 'NoMoreTelnet', 'NoMoreTelnet', true );
INSERT INTO game_level(id, attachments, estimated_duration, incorrect_flag_limit, content, flag, solution, solution_penalized) VALUES (13, null, 5, 5, '<style type="text/css">
  .prompt {color: #B7B7B7; font-weight: bold;}
  .command {color:white;}
</style>

<p>
  <b>Follow the tool!</b>
</p>

<ol style="margin-bottom: 2em">
  <li>Connect to the server:</li>
    <pre style="background-color:black; margin-top: 1em">
<span class="prompt">bnovak@workstation:~$ </span><span class="command">ssh bnovak@database.bigbroker.ex</span></pre>
  <li>Start the passcheck.py tool:</li>
    <pre style="background-color:black; margin-top: 1em">
<span class="prompt">bnovak@database:~$ </span><span class="command">./Tools/passcheck.py</span></pre>
  <li>Follow instructions...</li>
</ol>
', 'SetAndSafe', 'SetAndSafe', true );
INSERT INTO game_level(id, attachments, estimated_duration, incorrect_flag_limit, content, flag, solution, solution_penalized) VALUES (15, null, 15, 5, '<style type="text/css">
  .prompt {color: #B7B7B7; font-weight: bold;}
  .command {color:white;}
</style>

<p>
  <b>Just open the link!</b>
</p>

<ol style="margin-bottom: 2em">
  <li>Usage of the tool is described at <a href="https://tools.kali.org/password-attacks/hydra" target="_blank">https://tools.kali.org/password-attacks/hydra</a>:</li>
    <pre style="background-color:black; margin-top: 1em">
<span class="prompt">bnovak@workstation:~$ </span><span class="command">hydra -l vondrus -P ~/Downloads/500-worst-passwords.txt ssh://database.bigbroker.ex</span></pre>
  <li>After you retrieve credentials, connect to the central server as Vladimir and read the document.</li>
    <pre style="background-color:black; margin-top: 1em">
<span class="prompt">bnovak@workstation:~$ </span><span class="command">ssh vondrus@database.bigbroker.ex</span>
<span class="prompt">vondrus@database:~$ </span><span class="command">cat ~/Documents/company_employees.xml</span></pre>
</ol>', 'WhatAHacker', 'WhatAHacker', true );
INSERT INTO game_level(id, attachments, estimated_duration, incorrect_flag_limit, content, flag, solution, solution_penalized) VALUES (17, null, 5, 5, '<style type="text/css">
  .prompt {color: #B7B7B7; font-weight: bold;}
  .command {color:white;}
</style>

<p>
  <b>Follow the guide!</b>
</p>

<ol style="margin-bottom: 2em">
  <li>Create the SSH key pair:</li>
    <pre style="background-color:black; margin-top: 1em">
<span class="prompt">bnovak@workstation:~$ </span><span class="command">ssh-keygen -t rsa</span></pre>
  <li>Follow the instructions. (Do not change the file path and set a secure passphrase.)</li>
  <li>Upload the new key to the central server:</li>
    <pre style="background-color:black; margin-top: 1em">
<span class="prompt">bnovak@workstation:~$ </span><span class="command">ssh-copy-id bnovak@database.bigbroker.ex</span></pre>
  <li>Log in to the server and get content of the authorized_keys file:</li>
    <pre style="background-color:black; margin-top: 1em">
<span class="prompt">bnovak@workstation:~$ </span><span class="command">ssh bnovak@database.bigbroker.ex</span>
<span class="prompt">bnovak@database:~$ </span><span class="command">cat ~/.ssh/authorized_keys</span></pre>
</ol>', 'SSHKeys', 'SSHKeys', true );

INSERT INTO info_level(id, content) VALUES (4, 'Informational stuff');
INSERT INTO info_level(id, content) VALUES (5, 'Potatoes are not poisonous');
INSERT INTO info_level(id, content) VALUES (6, 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aenean vel tellus id orci interdum pulvinar et eu nisi. Suspendisse consequat, metus vel tempus placerat, eros nulla gravida enim, at suscipit nunc ex sit amet purus. Morbi scelerisque felis eget scelerisque ultricies. Aenean maximus, eros ac convallis tempus, ipsum ipsum venenatis magna, ac ullamcorper odio felis id augue. Donec tempus quis mauris quis sollicitudin. Donec et lorem porttitor, vulputate neque ut, sodales arcu. Phasellus iaculis dolor vel tincidunt vestibulum. Etiam dui neque, congue id aliquet et, blandit at diam. Ut purus orci, dapibus semper lobortis in, placerat id augue. Nam varius, ex sit amet viverra molestie, metus nibh ornare diam, in volutpat risus ligula eget ipsum. ');

INSERT INTO info_level(id, content) VALUES (10, '<style type="text/css">
  .prompt {color: #B7B7B7; font-weight: bold;}
  .command {color:white;}
</style>

<p>
  Damn computer error! <b>Network connection is slow</b>, and you are not able to sell stocks in time. Your network administrator <b>pretends that everything is fine</b> and advises you to turn your computer off and on again. Of course, that does not help and you must try to <b>fix it yourself</b>. Fortunately, you have a friend who is more familiar with information technology than you. So you call him immediately. After a while of thinking, he advises you to install the <i style="color: #008000">Wireshark</i> utility, run the packet capture, and send the result to him, just to make sure the network is working properly. Wireshark shows a lot of <b>TELNET</b> connections, but what is the biggest surprise? It contains <b>internal information</b> which should be accessible only to selected people...
</p>

<p>
  To start the packet capture, login to the Workstation (<b>username: <span style="color: #008000">bnovak</span></b>, <b>password: <span style="color: #008000">rabbit</span></b>), double-click on the Wireshark icon on the Desktop and run capture by clicking on the <b>"any"</b> network. Now you have access to <b>the content of internal reports</b> and even  <b>connection passwords!</b> Maybe it would be worthwhile to use some of the data for your profit before you report it to the administrator.
</p>

<p>
  Look at the packet capture and explore the internal reports. <b>The level flag is mentioned in the communication.</b>
</p>

<img src="https://is.muni.cz/www/cermmik/ctf-1-wireshark.png" style="display: block; margin-left: auto; margin-right: auto; margin-top: 1em; width: 40%;">

<p style="margin-top: 2em">
  <b>If you are stuck and need help, use the following hint:</b>
  <ul>
    <li>How to make the packet capture in the Wireshark more readable.</li>
  </ul>
</p>');
INSERT INTO info_level(id, content) VALUES (12, '<p>
  Thanks to your notice and threatening e-mail from your boss, network administrator forbids usage of the <b>insecure Telnet protocol</b> for connection to the company''s central server (<b>database.bigbroker.ex</b>). No one can read the sensitive internal reports anymore. From nowon, the <b>SSH protocol</b> will be used instead. This protocol provides an <b>encrypted connection</b> hiding all the transferred content. Everything will be allright now!
</p>

<p>
  The administrator has created <b>new server accounts with simple passwords</b> for all employees. Afterward, he sent an individual e-mail to each of them containing connection instruction together with a request on <b>immediate password change</b>. You received the e-mail as well:
</p>

<pre style="background: white; word-break: keep-all; width: 70%; margin-left: auto; margin-right: auto;">
From: administrator@bigbroker.ex
To: BohumirNovak@bigbroker.ex

Dear colleague,

our central server is much safer now! You can connect securely to the server using the "ssh" command (see a guide at https://www.ssh.com/ssh/command/). However, the upgrade brings small complications for you. I had to create new accounts for all users and set up new access passwords (see your password below). The provided password is usable only for the first login. Please change it immediately! For the sake of simplicity, I''ve created a simple tool to help you create and set up a new secure password. You can run the tool using the following command: "~/Tools/passcheck.py". I believe the server and all your connections will be secure now.

SSH username: bnovak
SSH password: scorpion

If you have any problem, do not hesitate to contact me.

Best regards,
  Milos Talas
</pre>

<p>
  That''s great news! Thanks to your efforts, internal information about stock trading are sufficiently secured. The last thing left is to connect to the central server and <b>change your password</b>. (Do not forget to remember your new password!) <span style="color: #008000">Use the tool provided by the administrator and change your password to a much secure one.</span>
</p>

<p>
  <b>The level flag is provided by the tool after the successful password change.</b>
</p>

<p style="margin-top: 3em">
  <b>If you are stuck and need help, use the following hint:</b>
  <ul>
    <li>How to connect to the server via SSH</li>
  </ul>
</p>');
INSERT INTO info_level(id, content) VALUES (14, '<p>
  Thanks to the repeated request for help, the administrator finally <b>fixed your computer</b> and you can trade stocks as you are used to. Right on time! One of your <b>biggest tradings is waiting for you</b>. Your colleague <b>Vladimir Ondrus</b> has been fired and all his work for Pear Inc., one of the most important corporate partners, was assigned to you! All trading is kept as secret as possible, but you need to know who is involved. <span style="color: #008000"> And of course, you can''t ask every single colleague.</span>
</p>

<p>
  Based on your previous cooperation, you know Vladimir has a <b>backup of all involved employees</b> in his Documents at the central server (<b>./Documents/company_employees.xml</b>). However, you do not have the <b>permissions to access the document</b>. Vladimir does not answers the phone, and the administrator is on vacation. Why now?! The trade time is approaching, and you really need the list. So you decide to try and <b>call your friend</b> again. Last time you were quite successful...
</p>

<img src="https://is.muni.cz/www/cermmik/ctf-1-hydra.jpg" style="display: block; margin-left: auto; margin-right: auto; margin-top: 1em;">

<p>
  Your friend recommends using a tool called <a href="https://tools.kali.org/password-attacks/hydra" target="_blank"><b>THC-Hydra</b></a> suitable for <b>cracking passwords of SSH</b> remote connection. You know that Vladimir doesn''t care too much about the security. Perhaps he was using a <b>weak password</b>! You immediately instal recommended tool on your <b>Workstation</b> and download the very first passwords dictionary you bump into (located in your <b>Downloads</b> directory). Now you just need to run the tool, retrieve the password and obtain the list of involved employees. <b>The level flag is at the top of the list.</b>
</p>

<p style="margin-top: 3em">
  <b>If you are stuck and need help, use the following hint:</b>
  <ul>
    <li>What is Vladimir''s username.</li>
    <li>How to set up the THC-Hydra if attack runs too slowly.</li>
  </ul>
</p>');
INSERT INTO info_level(id, content) VALUES (16, '<p>
  Thanks to your good friend, you were able to trade the stocks at the right time and <b>make a lot of money</b> for the Pear Inc. company. This will definitely be reflected in your salary! However, when the administrator returned from the vacation, the following <b>e-mail appeared in the mailbox</b> of all employees. <span style="color: #008000">You know very well that it''s related to your recent hacking activity...</span>
</p>

<pre style="background: white; word-break: keep-all; width: 70%; margin-left: auto; margin-right: auto;">
From: administrator@bigbroker.ex
To: internal@bigbroker.ex

Dear colleagues,

when I came back from my vacation, I discovered that someone was attacking our central server and tried to access sensitive data. Based on this experience, I have decided to increase the security level of authentication. From now on, we will use authentication based on SSH keys. Please follow the guide at the following link and set your SSH keys: https://www.digitalocean.com/community/tutorials/how-to-set-up-ssh-keys--2

After two weeks, I will disable password authentication. If you don''t upload your SSH key to the server, you will no longer be able to log in. Please check that your key was successfully uploaded and inserted into the ~/.ssh/authorized_keys file.

If you have any problems, do not hesitate to contact me.

Best regards,
  Milos Talas
</pre>

<p>
  It seems that nobody knows that you are the attacker. Lucky you! However, now you must <b>create SSH keys</b> and upload them to the server using the <a href="https://www.digitalocean.com/community/tutorials/how-to-set-up-ssh-keys--2" target="_blank"><b>referenced guide</b></a>. Try to log into the server after <b>successful key upload</b>. You will see that <i>ssh</i> tool will behave a little different. <b>The level flag is at the beginning of the ~/.ssh/authorized_keys file.</b>
</p>

<p style="margin-top: 3em">
  <b>If you are stuck and need help, use the following hint:</b>
  <ul>
    <li>On which computer you need to generate SSH keys</li>
  </ul>
</p>');


INSERT INTO assessment_level(id, assessment_type, instructions, questions) VALUES (7, 'TEST', 'Fill me up', 'What is my mothers name?');
INSERT INTO assessment_level(id, assessment_type, instructions, questions) VALUES (8, 'QUESTIONNAIRE', 'No rush', '...?');
INSERT INTO assessment_level(id, assessment_type, instructions, questions) VALUES (9, 'TEST', 'Fill me up', '[{"question_type":"FFQ","text":"Write name of one malicious software?","points":6,"penalty":3,"order":0,"answer_required":true,"correct_choices":["viruses","trojans","worms","bots"]},{"question_type":"MCQ","text":"Among the following choices, select all the possible methods of prevention against an unwanted file upload.","points":4,"penalty":2,"order":1,"answer_required":true,"choices":[{"order":0,"text":"whitelisting file extensions","is_correct":true},{"order":1,"text":"limiting maximum file size","is_correct":true},{"order":2,"text":"using database triggers","is_correct":false},{"order":3,"text":"saving data to an NTFS volume","is_correct":false}]},{"question_type":"EMI","text":"Connect the following exemplary situations with the corresponding type of password attack.","points":3,"penalty":1,"order":2,"answer_required":true,"choices":[{"order":0,"text":"trying all possible alphanumeric combinations of 8 characters","pair":6},{"order":1,"text":"trying common words of English language","pair":4},{"order":2,"text":"looking up the value of a hashed password","pair":7},{"order":3,"text":"tricking a user into giving away his password by posing as a service administrator","pair":5},{"order":4,"text":"dictionary attack","pair":1},{"order":5,"text":"social engineering","pair":3},{"order":6,"text":"brute force attack","pair":0},{"order":7,"text":"rainbow table attack","pair":2}]}]');

INSERT INTO hint(id, content, title, hint_penalty, game_level_id) VALUES (nextval('hint_id_seq'), 'Very good advice', 'Hint1', 10, 1);
INSERT INTO hint(id, content, title, hint_penalty, game_level_id) VALUES (nextval('hint_id_seq'), 'Very bad advice', 'Hint2', 6, 2);

INSERT INTO hint(id, content, title, hint_penalty, game_level_id) VALUES (nextval('hint_id_seq'), 'Right-click on any TELNET packet and select option <span style="color: #008000"><b>"Follow → TCP stream"</b></span>.', 'Hint1', 10, 11);
INSERT INTO hint(id, content, title, hint_penalty, game_level_id) VALUES (nextval('hint_id_seq'), 'Use the following command: <span style="color: #008000"><b>$ ssh bnovak@database.bigbroker.ex</b></span>.', 'Hint1', 10, 13);
INSERT INTO hint(id, content, title, hint_penalty, game_level_id) VALUES (nextval('hint_id_seq'), 'The same case as yours... His username is a combination of the first letter of the name and surname: <span style="color: #008000"><b>vondrus</b></span>.', 'Hint1', 15, 15);
INSERT INTO hint(id, content, title, hint_penalty, game_level_id) VALUES (nextval('hint_id_seq'), 'Use default settings of the THC-Hydra and <span style="color: #008000"><b>do not use "-t" option</b></span>.', 'Hint2', 5, 15);
INSERT INTO hint(id, content, title, hint_penalty, game_level_id) VALUES (nextval('hint_id_seq'), 'Your <b>private SSH key</b> must be securely stored so that no one has access to it. Therefore the keys need to be <span style="color: #008000"><b>generated on your workstation</b></span>.', 'Hint1', 10, 17);

INSERT INTO training_definition(id, description, outcomes, prerequisities, state, title, sandbox_definition_ref_id, starting_level, show_stepper_bar) VALUES (nextval('training_definition_id_seq'), 'Released training definition', null, null, 'RELEASED', 'TrainingDefinition1', 1, 4, true);
INSERT INTO training_definition(id, description, outcomes, prerequisities, state, title, sandbox_definition_ref_id, starting_level, show_stepper_bar) VALUES (nextval('training_definition_id_seq'), 'Unreleased training definition', null, null, 'UNRELEASED', 'TrainingDefinition2', 2, 5, false);
INSERT INTO training_definition(id, description, outcomes, prerequisities, state, title, sandbox_definition_ref_id, starting_level, show_stepper_bar) VALUES (nextval('training_definition_id_seq'), 'Released training definition2', null, null, 'RELEASED', 'TrainingDefinition2', 3, 6, true);
INSERT INTO training_definition(id, description, outcomes, prerequisities, state, title, sandbox_definition_ref_id, starting_level, show_stepper_bar) VALUES (nextval('training_definition_id_seq'), 'Introduction
============
<p>
  You are <b>a long-term employee of Big Broker Inc.</b>, one of the biggest trading companies in the world. After so many years, you are one of the main stock traders. <b>Stock trading is your life!</b> Reliable computer and fast Internet connection are essential premises of your success. Nevertheless, a computer is just a powerful working tool for you. You don''t know much about the technology and "cyber-security" is a strange word only. <b>However, it will change very quickly...</b>
</p>

<p>
  Your path to understanding the importance of cyber-security has started when your <b>computer broke</b>, and you were not able to sell your stocks in time. Money is the most important thing for you, and now <b>you''ve lost a lot of them</b> because of the stupid error! <span style="color: #008000">It''s time to take it into your own hands and fix it!</span>
</p>

<img src="https://is.muni.cz/www/cermmik/CTF-1-intro.jpg" style="display: block; margin-left: auto; margin-right: auto; width: 40%;">

<p style="margin-top: 2em;">
  You have access to the device with the <b>Xubuntu</b> operating system. This is a <b>common Linux distribution</b> enabling all the office activities you need. But it also provides many other interesting tools. <span style="color: #008000">Try to figure out how you can use these tools and fix your problem.</span>
</p>

<p style="margin-bottom: 3em;">
  <b>Press the "Start" button at the bottom of the page to start the game.</b>
</p>


Rules
=====
<p>
  <b>WARNING: You should never use these techniques and skills to gain unauthorized access. That is illegal. The game is for educational purposes only. The main purpose is to show you that absolute security does not exist, but you can learn how to defend yourself.</b>
</p>

<p>
  Click on the <b>Topology</b> button in the blue panel on the top to see the given network topology. When you double-click on the node called Big-Broker, you should see two nodes Database (main company server) and Workstation (common office client).
</p>

<p>
  Now, to gain remote access to your workstation computer click on Workstation and select <b>Remote connection</b>. A new tab appears with remote access to the workstation computer. Use the following credentials to login – <b>username: <span style="color: #008000">bnovak</span></b>, <b>password: <span style="color: #008000">rabbit</span></b>.
</p>

<p>
The game contains four levels. In each level, you will have to complete a specified task. To prove that you mastered the task, and so you are able to continue to the next level, you will have to submit a flag acquired in each task. Every level has a description of what to do and how to get the flag.
</p>

<p>
  General note: <span style="color: #008000">All programs which you will need to complete game tasks are installed (internet connection is not available). </span>
</p>


<h4 class="well well-small">Help System:</h4>

<p style="margin-bottom: 4em;">
  If you are unable to complete a level, you can use a <b>hint</b> which will give a little help, but you will lose some points. After using all the hints, there is <b>help level</b> prepared. It''s a guided solution which will tell you exactly what to do. The last option is to skip level entirely by clicking on the skip level, but this is not recommended – you won''t be able to complete the next levels without performing tasks in the current one.
</p>', null, null, 'RELEASED', 'CTF-1: No Secure Connection', null, 10, true);


INSERT INTO training_instance(id, pool_size, training_definition_id, start_time, end_time, access_token, title) VALUES (nextval('training_instance_id_seq'), 5, 1, '2016-10-19 10:23:54+02', '2017-10-19 10:23:54+02', 'pass-1235', 'Concluded Instance');
INSERT INTO training_instance(id, pool_size, training_definition_id, start_time, end_time, access_token, title) VALUES (nextval('training_instance_id_seq'), 8, 1, '2016-10-19 10:23:54+02', '2022-10-19 10:23:54+02', 'hello-6578', 'Current Instance');
INSERT INTO training_instance(id, pool_size, training_definition_id, start_time, end_time, access_token, title) VALUES (nextval('training_instance_id_seq'), 25, 1, '2020-10-19 10:23:54+02', '2024-10-19 10:23:54+02', 'keyword-9999', 'Future Instance');
INSERT INTO training_instance(id, pool_size, training_definition_id, start_time, end_time, access_token, title) VALUES (nextval('training_instance_id_seq'), 25, 3, '2017-10-19 10:23:54+02', '2024-10-19 10:23:54+02', 'keyword-1111', 'Future Instance');

INSERT INTO access_token(id, access_token) VALUES (nextval('access_token_id_seq'), 'pass-1235');
INSERT INTO access_token(id, access_token) VALUES (nextval('access_token_id_seq'), 'hello-6578');
INSERT INTO access_token(id, access_token) VALUES (nextval('access_token_id_seq'), 'keyword-9999');

INSERT INTO sandbox_instance_ref(id, sandbox_instance_ref, training_instance_id) VALUES (nextval('sandbox_instance_ref_id_seq'), 1, 1);
INSERT INTO sandbox_instance_ref(id, sandbox_instance_ref, training_instance_id) VALUES (nextval('sandbox_instance_ref_id_seq'), 2, 1);
INSERT INTO sandbox_instance_ref(id, sandbox_instance_ref, training_instance_id) VALUES (nextval('sandbox_instance_ref_id_seq'), 3, 2);
INSERT INTO sandbox_instance_ref(id, sandbox_instance_ref, training_instance_id) VALUES (nextval('sandbox_instance_ref_id_seq'), 4, 2);
INSERT INTO sandbox_instance_ref(id, sandbox_instance_ref, training_instance_id) VALUES (nextval('sandbox_instance_ref_id_seq'), 5, 3);
INSERT INTO sandbox_instance_ref(id, sandbox_instance_ref, training_instance_id) VALUES (nextval('sandbox_instance_ref_id_seq'), 6, 3);
INSERT INTO sandbox_instance_ref(id, sandbox_instance_ref, training_instance_id) VALUES (nextval('sandbox_instance_ref_id_seq'), 7, 4);
INSERT INTO sandbox_instance_ref(id, sandbox_instance_ref, training_instance_id) VALUES (nextval('sandbox_instance_ref_id_seq'), 8, 4);

INSERT INTO user_ref(id, user_ref_login) VALUES (nextval('user_ref_id_seq'),'Organizer1');
INSERT INTO user_ref(id, user_ref_login) VALUES (nextval('user_ref_id_seq'),'Organizer2');
INSERT INTO user_ref(id, user_ref_login) VALUES (nextval('user_ref_id_seq'),'Participant1');
INSERT INTO user_ref(id, user_ref_login) VALUES (nextval('user_ref_id_seq'),'Participant2');
INSERT INTO user_ref(id, user_ref_login) VALUES (nextval('user_ref_id_seq'), 'Designer1');
INSERT INTO user_ref(id, user_ref_login) VALUES (nextval('user_ref_id_seq'), 'Designer2');
INSERT INTO user_ref(id, user_ref_login) VALUES (nextval('user_ref_id_seq'), 'Designer3');

INSERT INTO training_instance_user_ref(training_instance_id, user_ref_id) VALUES (1, 1);
INSERT INTO training_instance_user_ref(training_instance_id, user_ref_id) VALUES (2, 1);
INSERT INTO training_instance_user_ref(training_instance_id, user_ref_id) VALUES (3, 2);
INSERT INTO training_definition_user_ref(training_definition_id, user_ref_id) VALUES (1, 5);
INSERT INTO training_definition_user_ref(training_definition_id, user_ref_id) VALUES (2, 6);
INSERT INTO training_definition_user_ref(training_definition_id, user_ref_id) VALUES (3, 7);


INSERT INTO training_run(id, start_time, end_time, state, current_level_id, sandbox_instance_ref_id, training_instance_id, user_ref_id, solution_taken, event_log_reference, incorrect_flag_count, assessment_responses, total_score, current_score, level_answered) VALUES (nextval('training_run_id_seq'),'2016-10-19 10:23:54+02', '2022-10-19 10:23:54+02', 'ALLOCATED', 4, 1, 1, 3, false, null, 0, '[]',30, 20, false);
INSERT INTO training_run(id, start_time, end_time, state, current_level_id, sandbox_instance_ref_id, training_instance_id, user_ref_id, solution_taken, event_log_reference, incorrect_flag_count, assessment_responses, total_score, current_score, level_answered) VALUES (nextval('training_run_id_seq'),'2020-10-19 10:23:54+02', '2024-10-19 10:23:54+02', 'ALLOCATED', 4, 5, 3, 4, false, null, 0, '[]', 100, 10, false);
INSERT INTO training_run(id, start_time, end_time, state, current_level_id, sandbox_instance_ref_id, training_instance_id, user_ref_id, solution_taken, event_log_reference, incorrect_flag_count, assessment_responses, total_score, current_score, level_answered) VALUES (nextval('training_run_id_seq'),'2019-10-19 10:23:54+02', '2024-10-19 10:23:54+02', 'ALLOCATED', 6, 7, 4, 3, false, null, 0, '[]', 0, 0, true);
