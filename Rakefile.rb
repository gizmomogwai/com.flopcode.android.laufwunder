desc 'copy all workouts to the device'
task :copy_to_device do
  Dir.glob('data/*.workout').each do |f|
    sh "adb push #{f} /sdcard/laufwunder/workouts"
  end
end

task :default => :copy_to_device
