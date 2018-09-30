# -*- mode: ruby -*-
# vi: set ft=ruby :

repos = [
    'git@gitlab.ics.muni.cz:kypo2/services-and-portlets/kypo2-security-commons.git',
    'git@gitlab.ics.muni.cz:kypo2/services-and-portlets/kypo2-user-and-group.git',
]
roles = [
    {
        'name': 'postgresql',
        'url': 'git@gitlab.ics.muni.cz:CSIRT-MU-public/ansible-roles/postgresql.git'
    },
    {
        'name': 'jradtilbrook.elasticsearch',
        'url': 'https://github.com/jradtilbrook/ansible-role-elasticsearch.git'
    },
    {
        'name': 'jradtilbrook.kibana',
        'url': 'https://github.com/jradtilbrook/ansible-role-kibana.git'
    },
]

def git(url, dir)
    if File.directory?(File.join(dir, '.git'))
        system('cd #{dir} && git checkout master && git pull')
    else
        command = 'git clone'
        puts "Cloning '#{url}'..."
        %x(#{command} #{url} #{dir})
    end
end

ansible_dir = 'ansible'

repos_dir = File.join(ansible_dir, 'gitlab')
Dir.mkdir(repos_dir) unless File.directory?(repos_dir)

roles_dir = File.join(ansible_dir, 'roles')
Dir.mkdir(roles_dir) unless File.directory?(roles_dir)

# All Vagrant configuration is done below. The "2" in Vagrant.configure
# configures the configuration version (we support older styles for
# backwards compatibility). Please don't change it unless you know what
# you're doing.
Vagrant.configure('2') do |config|
    config.vm.box = 'ubuntu/trusty64'

    config.vm.network 'forwarded_port', guest: 9200, host: 9200
    config.vm.network 'forwarded_port', guest: 9201, host: 9201
    config.vm.network 'forwarded_port', guest: 5601, host: 5601
    config.vm.network 'forwarded_port', guest: 8080, host: 8080

    config.vm.provider 'virtualbox' do |v|
        v.memory = 5120
        # v.cpus = 2
    end

    config.vm.provision 'ansible_local' do |ansible|
        repos.each do |repo|
            dir = File.join(repos_dir, File.basename(repo, '.*'))
            git(repo, dir)
        end

        roles.each do |role|
            dir = File.join(roles_dir, role[:name])
            git(role[:url], dir)
        end

        #ansible.verbose = 'v'
        ansible.playbook = File.join(ansible_dir, 'playbook.yml')
        ansible.extra_vars = {
            'repos_dir': File.join('/vagrant', repos_dir)
        }
    end
end
