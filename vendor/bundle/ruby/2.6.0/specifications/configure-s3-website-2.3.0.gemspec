# -*- encoding: utf-8 -*-
# stub: configure-s3-website 2.3.0 ruby lib

Gem::Specification.new do |s|
  s.name = "configure-s3-website".freeze
  s.version = "2.3.0"

  s.required_rubygems_version = Gem::Requirement.new(">= 0".freeze) if s.respond_to? :required_rubygems_version=
  s.require_paths = ["lib".freeze]
  s.authors = ["Lauri Lehmijoki".freeze]
  s.date = "2017-10-11"
  s.email = "lauri.lehmijoki@iki.fi".freeze
  s.executables = ["configure-s3-website".freeze]
  s.files = ["bin/configure-s3-website".freeze]
  s.homepage = "https://github.com/laurilehmijoki/configure-s3-website".freeze
  s.rubygems_version = "3.0.3".freeze
  s.summary = "Configure your AWS S3 bucket to function as a web site".freeze

  s.installed_by_version = "3.0.3" if s.respond_to? :installed_by_version

  if s.respond_to? :specification_version then
    s.specification_version = 4

    if Gem::Version.new(Gem::VERSION) >= Gem::Version.new('1.2.0') then
      s.add_runtime_dependency(%q<deep_merge>.freeze, ["~> 1.0.0"])
      s.add_runtime_dependency(%q<aws-sdk>.freeze, ["~> 2"])
      s.add_development_dependency(%q<rspec>.freeze, ["~> 3"])
      s.add_development_dependency(%q<rspec-expectations>.freeze, ["~> 3"])
      s.add_development_dependency(%q<rake>.freeze, ["~> 0.9.0"])
    else
      s.add_dependency(%q<deep_merge>.freeze, ["~> 1.0.0"])
      s.add_dependency(%q<aws-sdk>.freeze, ["~> 2"])
      s.add_dependency(%q<rspec>.freeze, ["~> 3"])
      s.add_dependency(%q<rspec-expectations>.freeze, ["~> 3"])
      s.add_dependency(%q<rake>.freeze, ["~> 0.9.0"])
    end
  else
    s.add_dependency(%q<deep_merge>.freeze, ["~> 1.0.0"])
    s.add_dependency(%q<aws-sdk>.freeze, ["~> 2"])
    s.add_dependency(%q<rspec>.freeze, ["~> 3"])
    s.add_dependency(%q<rspec-expectations>.freeze, ["~> 3"])
    s.add_dependency(%q<rake>.freeze, ["~> 0.9.0"])
  end
end
